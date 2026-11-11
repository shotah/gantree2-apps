import Foundation

#if canImport(FoundationNetworking)
  import FoundationNetworking
#endif

public enum TtsResult: Equatable {
  case ok(Data)
  case err(SpeakFail)
}

/// The Worker route that speaks a reply for one room.
///
/// - Parameters:
///   - origin: The gantree origin, HTTP or socket form.
///   - slug: The room the reply belongs to. The Worker admits the caller against it.
/// - Returns: `<http origin>/api/chat/tts?slug=<slug>`.
public func ttsUrl(_ origin: String, slug: String) -> String {
  "\(httpOrigin(origin))/api/chat/tts?slug=\(slug)"
}

public final class TtsApi {
  private let transport: HTTPTransport

  public init(transport: HTTPTransport) {
    self.transport = transport
  }

  /// Asks the Worker to speak `text` for a room.
  ///
  /// - Parameters:
  ///   - origin: The gantree origin.
  ///   - slug: The room the reply belongs to.
  ///   - bearer: The session sent as `Authorization`. Blank sends none.
  ///   - text: The words to speak, already stripped of markdown and clipped.
  ///   - lang: The Settings language id. An unknown id falls back to the default.
  /// - Returns: The MP3 bytes, or why there are none: the HTTP status mapped by
  ///   `speakFailFromStatus`, `.vendor` for an empty body, `.offline` when the request failed.
  public func synthesize(
    origin: String, slug: String, bearer: String, text: String, lang: String = defaultLang
  ) -> TtsResult {
    let payload = JSON.stringify(["text": text, "lang": parseLang(lang)])
    var req = URLRequest(url: URL(string: ttsUrl(origin, slug: slug))!)
    req.httpMethod = "POST"
    req.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
    req.httpBody = payload.data(using: .utf8)
    if !bearer.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
      req.setValue("Bearer \(bearer)", forHTTPHeaderField: "Authorization")
    }
    do {
      let res = try transport.perform(req)
      if res.status < 200 || res.status >= 300 {
        return .err(speakFailFromStatus(res.status))
      }
      if res.body.isEmpty {
        return .err(.vendor)
      }
      return .ok(res.body)
    } catch {
      return .err(.offline)
    }
  }
}
