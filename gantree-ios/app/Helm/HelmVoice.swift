import Foundation
import Mailbox

#if canImport(Speech)
  import Speech
#endif
#if canImport(AVFoundation)
  import AVFoundation
#endif

/// Pocket voice. A hold this phone armed reads the next live `reply` through
/// `POST /api/chat/tts?slug=<room>`. CarPlay keeps the notification card; nothing here speaks in the car.
final class HelmVoice: NSObject {
  var onWords: ((String) -> Void)?
  var onPhase: ((SpeakPhase) -> Void)?
  var onFail: ((String) -> Void)?
  var onBlocked: (() -> Void)?

  private let tts = TtsApi(transport: URLSessionTransport())
  private var armed = false
  private var utterance: Utterance?
  /// Bumped by every speak and hush; a fetched clip plays only if its turn is still current.
  private var speechTurn = 0
  #if canImport(Speech)
    private let engine = AVAudioEngine()
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?
  #endif
  #if canImport(AVFoundation)
    private var player: AVAudioPlayer?
  #endif

  func arm() {
    armed = true
  }

  /// Reads the frame aloud when it is the live reply to a hold this phone armed.
  ///
  /// - Parameters:
  ///   - frame: A mailbox frame, after `Mouth.ingest`.
  ///   - fresh: Whether `Mouth.ingest` painted it as a new turn.
  ///   - origin: The gantree HTTP origin.
  ///   - slug: The room the frame arrived on; the Worker admits the speech request against it.
  ///   - bearer: The session or spike sent as `Authorization`.
  ///   - lang: The Settings language id the reply is spoken in.
  func heard(
    frame: WireFrame, fresh: Bool, origin: String, slug: String, bearer: String, lang: String
  ) {
    if disarmsVoice(frame.kind) {
      armed = false
    }
    guard speaksReply(kind: frame.kind, replay: frame.replay, fresh: fresh, armed: armed) else {
      return
    }
    armed = false
    speak(frame.text ?? "", origin: origin, slug: slug, bearer: bearer, lang: lang)
  }

  /// Quiets the speaker before the mic opens, including a reply still being fetched.
  func hush() {
    speechTurn += 1
    #if canImport(AVFoundation)
      player?.stop()
      player = nil
    #endif
    onPhase?(.idle)
  }

  func begin(lang: String) {
    utterance = Utterance { [weak self] words in
      self?.onWords?(words)
    }
    #if canImport(Speech)
      SFSpeechRecognizer.requestAuthorization { [weak self] status in
        DispatchQueue.main.async {
          guard status == .authorized else {
            self?.onBlocked?()
            return
          }
          self?.startEngine(lang: lang)
        }
      }
    #else
      onBlocked?()
    #endif
  }

  func finish() {
    stopEngine()
    utterance?.finish()
    utterance = nil
  }

  func abort() {
    stopEngine()
    utterance?.abort()
    utterance = nil
  }

  /// Fetches the reply's speech for the room and plays it, unless a hush or a later reply came first.
  private func speak(_ markdown: String, origin: String, slug: String, bearer: String, lang: String)
  {
    speechTurn += 1
    let turn = speechTurn
    let text = clipForSpeech(speakable(markdown))
    if text.isEmpty {
      onPhase?(.idle)
      return
    }
    onPhase?(.fetching)
    DispatchQueue.global(qos: .userInitiated).async {
      let got = self.tts.synthesize(
        origin: origin, slug: slug, bearer: bearer, text: text, lang: lang)
      DispatchQueue.main.async {
        guard turn == self.speechTurn else {
          return
        }
        switch got {
        case .err(let reason):
          self.onPhase?(.idle)
          let why = speakFailHint(reason)
          if !why.isEmpty {
            self.onFail?(why)
          }
        case .ok(let data):
          self.play(data)
        }
      }
    }
  }

  private func play(_ data: Data) {
    #if canImport(AVFoundation)
      do {
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
        try session.setActive(true)
        let player = try AVAudioPlayer(data: data)
        player.delegate = self
        self.player = player
        player.play()
        onPhase?(.playing)
      } catch {
        onPhase?(.idle)
        onFail?(speakFailHint(.play))
      }
    #else
      onPhase?(.idle)
      onFail?(speakFailHint(.play))
    #endif
  }

  #if canImport(Speech)
    /// Opens the mic and streams it to the recognizer. The session switches to a recording
    /// category first: after a spoken reply it is still in `.playback`, which has no input.
    private func startEngine(lang: String) {
      stopEngine()
      let recognizer = SFSpeechRecognizer(locale: Locale(identifier: speechLang(lang)))
      guard let recognizer, recognizer.isAvailable else {
        onBlocked?()
        return
      }
      do {
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.record, mode: .measurement, options: [.duckOthers])
        try session.setActive(true)
      } catch {
        onBlocked?()
        return
      }
      let request = SFSpeechAudioBufferRecognitionRequest()
      request.shouldReportPartialResults = true
      self.request = request
      let input = engine.inputNode
      let format = input.outputFormat(forBus: 0)
      input.installTap(onBus: 0, bufferSize: 1024, format: format) { buffer, _ in
        request.append(buffer)
      }
      engine.prepare()
      do {
        try engine.start()
      } catch {
        onBlocked?()
        return
      }
      task = recognizer.recognitionTask(with: request) { [weak self] result, _ in
        guard let result else {
          return
        }
        if result.isFinal {
          self?.utterance?.final(result.bestTranscription.formattedString)
        } else {
          self?.utterance?.partial(result.bestTranscription.formattedString)
        }
      }
    }
  #endif

  /// Closes the mic and ends recognition. The tap comes off even when the engine never
  /// started, since a second tap on the same bus is fatal.
  private func stopEngine() {
    #if canImport(Speech)
      if engine.isRunning {
        engine.stop()
      }
      engine.inputNode.removeTap(onBus: 0)
      request?.endAudio()
      task?.cancel()
      request = nil
      task = nil
    #endif
  }
}

#if canImport(AVFoundation)
  extension HelmVoice: AVAudioPlayerDelegate {
    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
      if !flag {
        onFail?(speakFailHint(.play))
      }
      onPhase?(.idle)
    }
  }
#endif
