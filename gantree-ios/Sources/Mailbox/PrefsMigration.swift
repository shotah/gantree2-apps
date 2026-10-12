import Foundation

/// Bare UserDefaults keys copied from the "helm" suite into "gantree".
/// Per-room theme rows use `prefsRoomThemePrefix` plus the slug.
public let prefsKeys = [
  "origin", "slug", "spike", "session", "session_exp", "email", "gps",
  "theme", "font", "photo", "backdrop", "followTheme",
  "geo_lat", "geo_lon", "geo_acc", "voice", "voiceOffered", "lang",
  "aimsSeen", "todoSeen",
]

public let prefsRoomThemePrefix = "roomTheme."

/// On-disk paths under Application Support. Legacy paths are the pre-rename copies.
public let prefsThreadRelative = "gantree/thread.json"
public let prefsBlobsRelative = "gantree/blobs"
public let prefsLegacyThreadRelative = "helm/thread.json"
public let prefsLegacyBlobsRelative = "helm/blobs"

/// Copy the old suite once. Nil when the new suite already has origin or slug,
/// or when the old suite has none of the known keys (including `roomTheme.<slug>`).
public func migratePrefs(old: [String: Any], newHasOriginOrSlug: Bool) -> [String: Any]? {
  if newHasOriginOrSlug {
    return nil
  }
  var copied: [String: Any] = [:]
  for key in prefsKeys {
    if let value = old[key] {
      copied[key] = value
    }
  }
  for (key, value) in old
  where key.hasPrefix(prefsRoomThemePrefix) && key.count > prefsRoomThemePrefix.count {
    copied[key] = value
  }
  return copied.isEmpty ? nil : copied
}

/// Copy the old file or directory onto the new path only while the new one is absent.
public func shouldCopyPrefsPath(oldExists: Bool, newExists: Bool) -> Bool {
  oldExists && !newExists
}
