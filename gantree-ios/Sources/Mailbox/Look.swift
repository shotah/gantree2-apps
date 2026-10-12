/// Settings groups. Feel `neutral` is Plain. Each other feel is one Moods row.
public struct ThemeGroup: Equatable {
  public var title: String
  public var rows: [[String]]
}

/// Ids and order from `GantreeThemes`. Unknown ids fall back to boom.
public let themeGroups: [ThemeGroup] = themeGroupsFromCatalog()

public let themeIds: [String] = GantreeThemes.ids

public let defaultTheme = GantreeThemes.defaultId

public let fontIds = ["sm", "md", "lg", "xl"]
public let defaultFont = "sm"

public func knownTheme(_ v: String?) -> String? {
  guard let v, themeIds.contains(v) else {
    return nil
  }
  return v
}

public func parseTheme(_ v: String?) -> String {
  GantreeThemes.of(v).id
}

public func parseFont(_ v: String?) -> String {
  guard let v, fontIds.contains(v) else {
    return defaultFont
  }
  return v
}

public func chatSp(_ fontId: String) -> Float {
  switch parseFont(fontId) {
  case "md": return 16
  case "lg": return 20
  case "xl": return 24
  default: return 14
  }
}

public func themeLabel(_ id: String) -> String {
  GantreeThemes.of(id).label
}

public func fontLabel(_ id: String) -> String {
  switch parseFont(id) {
  case "md": return "Medium"
  case "lg": return "Large"
  case "xl": return "Extra large"
  default: return "Small"
  }
}

/// What to paint. Follow Kit when the room has a known id; empty / junk / follow-off
/// keeps the human's pick. Same rule as gantree `THEME_BOOT`.
public func paintedTheme(follow: Bool, roomTheme: String, mine: String) -> String {
  if !follow {
    return parseTheme(mine)
  }
  return knownTheme(roomTheme) ?? parseTheme(mine)
}

/// Header face — same numbers as gantree `KitAvatar` xl and gantree-android `KitAvatar`.
public let headerFaceSize: Double = 82
public let headerFaceSlotWidth: Double = 80
public let headerFaceSlotHeight: Double = 40
public let headerFaceNudgeX: Double = -2
public let headerFaceNudgeY: Double = -4
public let headerFaceStroke: Double = 2

private func themeGroupsFromCatalog() -> [ThemeGroup] {
  let themes = GantreeThemes.ids.map { GantreeThemes.of($0) }
  let plain = themes.filter { $0.feel == "neutral" }.map(\.id)
  var moodRows: [[String]] = []
  var seen: [String] = []
  for theme in themes where theme.feel != "neutral" {
    if seen.contains(theme.feel) {
      continue
    }
    seen.append(theme.feel)
    moodRows.append(themes.filter { $0.feel == theme.feel }.map(\.id))
  }
  return [
    ThemeGroup(title: "Plain", rows: [plain]),
    ThemeGroup(title: "Moods", rows: moodRows),
  ]
}
