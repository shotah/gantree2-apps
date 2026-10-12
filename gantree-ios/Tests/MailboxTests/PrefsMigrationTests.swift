import XCTest

@testable import Mailbox

final class PrefsMigrationTests: XCTestCase {
  func testMigratePrefsCopiesKnownKeysOnce() {
    let old: [String: Any] = [
      "origin": "https://gantree.example.com",
      "slug": "kit",
      "theme": "paper",
      "roomTheme.kit": "siren",
      "junk": "no",
    ]
    let got = migratePrefs(old: old, newHasOriginOrSlug: false)!
    XCTAssertEqual("https://gantree.example.com", got["origin"] as? String)
    XCTAssertEqual("kit", got["slug"] as? String)
    XCTAssertEqual("paper", got["theme"] as? String)
    XCTAssertEqual("siren", got["roomTheme.kit"] as? String)
    XCTAssertNil(got["junk"])
    XCTAssertEqual(4, got.count)
    XCTAssertNil(migratePrefs(old: old, newHasOriginOrSlug: true))
    XCTAssertNil(migratePrefs(old: ["junk": "x"], newHasOriginOrSlug: false))
  }

  func testMigratePrefsSkipsWhenTheNewSuiteAlreadyHasASlug() {
    let old: [String: Any] = ["origin": "https://gantree.example.com", "theme": "ink"]
    XCTAssertNil(migratePrefs(old: old, newHasOriginOrSlug: true))
  }

  func testPrefsPathsCopyOnce() {
    XCTAssertEqual("gantree/thread.json", prefsThreadRelative)
    XCTAssertEqual("gantree/blobs", prefsBlobsRelative)
    XCTAssertEqual("helm/thread.json", prefsLegacyThreadRelative)
    XCTAssertEqual("helm/blobs", prefsLegacyBlobsRelative)
    XCTAssertTrue(shouldCopyPrefsPath(oldExists: true, newExists: false))
    XCTAssertFalse(shouldCopyPrefsPath(oldExists: true, newExists: true))
    XCTAssertFalse(shouldCopyPrefsPath(oldExists: false, newExists: false))
    XCTAssertFalse(shouldCopyPrefsPath(oldExists: false, newExists: true))
  }
}
