/// Hexes come from `GantreeThemes` (the shared catalog). Values are 0xRRGGBB
/// (opaque). SwiftUI maps them in the app target. `scheme` drives preferredColorScheme.
public struct HelmColors: Equatable {
  public var canvas: UInt32
  public var panel: UInt32
  public var track: UInt32
  public var line: UInt32
  public var edge: UInt32
  public var fg: UInt32
  public var body: UInt32
  public var muted: UInt32
  public var dim: UInt32
  public var accent: UInt32
  public var mark: UInt32
  public var accentLine: UInt32
  public var accentSoft: UInt32
  public var danger: UInt32
  public var ok: UInt32
  public var you: UInt32
  public var kit: UInt32
  public var scheme: String
}

public func helmColors(_ themeId: String) -> HelmColors {
  let t = GantreeThemes.of(themeId)
  return HelmColors(
    canvas: t.canvas, panel: t.panel, track: t.track, line: t.line, edge: t.edge,
    fg: t.fg, body: t.body, muted: t.muted, dim: t.dim, accent: t.accent,
    mark: t.mark, accentLine: t.accentLine, accentSoft: t.accentSoft, danger: t.danger,
    ok: t.ok, you: t.you, kit: t.kit, scheme: t.scheme
  )
}

public func boomColors() -> HelmColors {
  helmColors(GantreeThemes.defaultId)
}
