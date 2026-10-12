import XCTest

@testable import Mailbox

final class LookAvatarPaletteTests: XCTestCase {
  func testThemeFallsBackToBoom() {
    XCTAssertEqual(defaultTheme, parseTheme(nil))
    XCTAssertEqual("boom", parseTheme("nope"))
    XCTAssertEqual("boom", parseTheme("noir"))
    XCTAssertEqual("boom", parseTheme("lamp"))
    XCTAssertEqual(
      [
        "boom", "paper", "ink",
        "marquee", "lemonade",
        "neon", "fizz",
        "rain", "mist",
        "fuse", "grit",
        "siren", "flare",
        "static", "flicker",
      ],
      themeIds
    )
    XCTAssertEqual(["Plain", "Moods"], themeGroups.map(\.title))
    XCTAssertEqual([["boom", "paper", "ink"]], themeGroups[0].rows)
    XCTAssertEqual(
      [
        ["marquee", "lemonade"],
        ["neon", "fizz"],
        ["rain", "mist"],
        ["fuse", "grit"],
        ["siren", "flare"],
        ["static", "flicker"],
      ],
      themeGroups[1].rows
    )
    XCTAssertEqual("paper", parseTheme("paper"))
    XCTAssertEqual("ink", knownTheme("ink"))
    XCTAssertEqual("siren", knownTheme("siren"))
    XCTAssertNil(knownTheme("noir"))
    XCTAssertNil(knownTheme("nope"))
  }

  func testFollowPaintsTheRoomThemeUntilItIsCleared() {
    XCTAssertEqual("siren", paintedTheme(follow: true, roomTheme: "siren", mine: "boom"))
    XCTAssertEqual("paper", paintedTheme(follow: true, roomTheme: "noir", mine: "paper"))
    XCTAssertEqual("boom", paintedTheme(follow: true, roomTheme: "", mine: "boom"))
    XCTAssertEqual("boom", paintedTheme(follow: true, roomTheme: "nope", mine: "boom"))
    XCTAssertEqual("paper", paintedTheme(follow: false, roomTheme: "neon", mine: "paper"))
  }

  func testFontFallsBackToSmall() {
    XCTAssertEqual(defaultFont, parseFont(nil))
    XCTAssertEqual("sm", parseFont("nope"))
    XCTAssertEqual("lg", parseFont("lg"))
    XCTAssertEqual(["sm", "md", "lg", "xl"], fontIds)
    XCTAssertEqual(14, chatSp("sm"), accuracy: 0.01)
    XCTAssertEqual(16, chatSp("md"), accuracy: 0.01)
    XCTAssertEqual(20, chatSp("lg"), accuracy: 0.01)
    XCTAssertEqual(24, chatSp("xl"), accuracy: 0.01)
    XCTAssertEqual(14, chatSp("nope"), accuracy: 0.01)
  }

  func testLabelsMatchTheCatalog() {
    XCTAssertEqual("Boom", themeLabel("boom"))
    XCTAssertEqual("Boom", themeLabel("noir"))
    XCTAssertEqual("Paper", themeLabel("paper"))
    XCTAssertEqual("Ink", themeLabel("ink"))
    XCTAssertEqual("Marquee", themeLabel("marquee"))
    XCTAssertEqual("Lemonade", themeLabel("lemonade"))
    XCTAssertEqual("Neon", themeLabel("neon"))
    XCTAssertEqual("Fizz", themeLabel("fizz"))
    XCTAssertEqual("Rain", themeLabel("rain"))
    XCTAssertEqual("Mist", themeLabel("mist"))
    XCTAssertEqual("Fuse", themeLabel("fuse"))
    XCTAssertEqual("Grit", themeLabel("grit"))
    XCTAssertEqual("Siren", themeLabel("siren"))
    XCTAssertEqual("Flare", themeLabel("flare"))
    XCTAssertEqual("Static", themeLabel("static"))
    XCTAssertEqual("Flicker", themeLabel("flicker"))
    XCTAssertEqual("Small", fontLabel("sm"))
    XCTAssertEqual("Medium", fontLabel("md"))
    XCTAssertEqual("Large", fontLabel("lg"))
    XCTAssertEqual("Extra large", fontLabel("xl"))
  }

  func testHeaderFaceMatchesGantreeAndroid() {
    XCTAssertEqual(82, headerFaceSize, accuracy: 0.01)
    XCTAssertEqual(80, headerFaceSlotWidth, accuracy: 0.01)
    XCTAssertEqual(40, headerFaceSlotHeight, accuracy: 0.01)
    XCTAssertEqual(-2, headerFaceNudgeX, accuracy: 0.01)
    XCTAssertEqual(-4, headerFaceNudgeY, accuracy: 0.01)
    XCTAssertEqual(2, headerFaceStroke, accuracy: 0.01)
  }

  func testDisplaySlugTitleCases() {
    XCTAssertEqual("Kit", displaySlug(""))
    XCTAssertEqual("Kit", displaySlug("  "))
    XCTAssertEqual("Kit", displaySlug("kit"))
    XCTAssertEqual("Ada", displaySlug("ada"))
  }

  func testBlobEtagAndRevRoundTrip() {
    XCTAssertEqual("\"7\"", blobEtag(7))
    XCTAssertEqual(7, blobRev("7", etag: nil))
    XCTAssertEqual(7, blobRev(nil, etag: "\"7\""))
    XCTAssertEqual(7, blobRev(nil, etag: "W/\"7\""))
    XCTAssertEqual(12, blobRev("12", etag: "\"7\""))
    XCTAssertEqual(0, blobRev(nil, etag: nil))
    XCTAssertEqual(0, blobRev("0", etag: "\"-3\""))
    XCTAssertEqual(0, blobRev("nope", etag: "\"abc\""))
  }

  func testFaceRevOnlyFromFaceKind() {
    XCTAssertEqual(9, faceRev("face", "9"))
    XCTAssertNil(faceRev("reply", "9"))
    XCTAssertNil(faceRev("face", "nope"))
    XCTAssertNil(faceRev("face", "0"))
    XCTAssertNil(faceRev("face", "-1"))
    XCTAssertNil(faceRev("face", nil))
  }

  func testAvatarUrlPinsARev() {
    XCTAssertEqual(
      "https://example.workers.dev/api/avatar?slug=kit",
      avatarUrl("https://example.workers.dev/", slug: "kit")
    )
    XCTAssertEqual(
      "https://example.workers.dev/api/avatar?slug=kit&v=3",
      avatarUrl("https://example.workers.dev/", slug: "kit", rev: 3)
    )
  }

  func testBackdropAndThemeUrlsMatchTheWorker() {
    XCTAssertEqual(
      "https://example.workers.dev/api/backdrop?slug=kit",
      backdropUrl("https://example.workers.dev/", slug: "kit")
    )
    XCTAssertEqual(
      "https://example.workers.dev/api/backdrop?slug=kit&v=9",
      backdropUrl("https://example.workers.dev/", slug: "kit", rev: 9)
    )
    XCTAssertEqual(
      "https://example.workers.dev/api/theme?slug=kit",
      themeUrl("https://example.workers.dev/", slug: "kit")
    )
  }

  func testBackdropRevAllowsZeroAndDropsJunk() {
    XCTAssertEqual(0, backdropRev("backdrop", 0))
    XCTAssertEqual(9, backdropRev("backdrop", 9))
    XCTAssertNil(backdropRev("backdrop", -1))
    XCTAssertNil(backdropRev("backdrop", 1.5))
    XCTAssertNil(backdropRev("face", 9))
    XCTAssertNil(backdropRev("backdrop", nil as Any?))
  }

  func testRoomThemeNoticeClearsOnNullAndIgnoresJunk() {
    XCTAssertEqual(
      "marquee",
      roomThemeNotice("theme", themePresent: true, themeNull: false, themeRaw: "marquee"))
    XCTAssertEqual(
      "", roomThemeNotice("theme", themePresent: true, themeNull: true, themeRaw: "marquee"))
    XCTAssertEqual(
      "", roomThemeNotice("theme", themePresent: false, themeNull: false, themeRaw: nil))
    XCTAssertNil(roomThemeNotice("theme", themePresent: true, themeNull: false, themeRaw: "noir"))
    XCTAssertNil(roomThemeNotice("face", themePresent: true, themeNull: false, themeRaw: "marquee"))
  }

  func testPaletteMatchesTheCatalogBlocks() throws {
    let root = try sharedThemeTokens()
    let themes = root["themes"] as! [[String: Any]]
    let ids = themes.map { $0["id"] as! String }
    XCTAssertEqual(ids, themeIds)
    XCTAssertEqual(root["default"] as? String, defaultTheme)
    XCTAssertEqual(GantreeThemes.defaultId, defaultTheme)
    XCTAssertEqual(boomColors(), helmColors("nope"))
    XCTAssertEqual(boomColors(), helmColors("noir"))
    XCTAssertEqual(["Plain", "Moods"], themeGroups.map(\.title))

    let plain = themes.filter { ($0["feel"] as? String) == "neutral" }.map { $0["id"] as! String }
    XCTAssertEqual([plain], themeGroups[0].rows)
    var moodRows: [[String]] = []
    var seen: [String] = []
    for theme in themes {
      let feel = theme["feel"] as! String
      if feel == "neutral" || seen.contains(feel) {
        continue
      }
      seen.append(feel)
      let row = themes.filter { ($0["feel"] as? String) == feel }.map { $0["id"] as! String }
      moodRows.append(row)
    }
    XCTAssertEqual(moodRows, themeGroups[1].rows)

    let fields = [
      "canvas", "panel", "track", "line", "edge", "fg", "body", "muted", "dim", "accent",
      "mark", "accentLine", "accentSoft", "danger", "ok", "you", "kit",
    ]
    for theme in themes {
      let id = theme["id"] as! String
      let tokens = theme["tokens"] as! [String: Any]
      let got = helmColors(id)
      XCTAssertEqual(tokens["scheme"] as? String, got.scheme, id)
      for field in fields {
        let hex = tokens[field] as! String
        let want = UInt32(hex.dropFirst(), radix: 16)!
        XCTAssertEqual(want, helmField(got, field), "\(id).\(field)")
      }
    }
  }
}

private func sharedThemeTokens() throws -> [String: Any] {
  var dir = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
  let fm = FileManager.default
  while dir.path != "/" {
    let candidate = dir.appendingPathComponent("shared/theme/tokens.json")
    if fm.fileExists(atPath: candidate.path) {
      let data = try Data(contentsOf: candidate)
      let obj = try JSONSerialization.jsonObject(with: data)
      return obj as! [String: Any]
    }
    dir.deleteLastPathComponent()
  }
  throw NSError(
    domain: "LookAvatarPaletteTests", code: 1,
    userInfo: [
      NSLocalizedDescriptionKey: "shared/theme/tokens.json not found above \(#filePath)"
    ])
}

private func helmField(_ colors: HelmColors, _ name: String) -> UInt32 {
  switch name {
  case "canvas": return colors.canvas
  case "panel": return colors.panel
  case "track": return colors.track
  case "line": return colors.line
  case "edge": return colors.edge
  case "fg": return colors.fg
  case "body": return colors.body
  case "muted": return colors.muted
  case "dim": return colors.dim
  case "accent": return colors.accent
  case "mark": return colors.mark
  case "accentLine": return colors.accentLine
  case "accentSoft": return colors.accentSoft
  case "danger": return colors.danger
  case "ok": return colors.ok
  case "you": return colors.you
  case "kit": return colors.kit
  default: return 0
  }
}
