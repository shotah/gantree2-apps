# Todo

Open work for both apps. Ordered so each item is safe to do alone.
Per-app detail that predates this repo is still accurate and lives in
[`gantree-android/docs/todo.md`](../gantree-android/docs/todo.md) and
[`gantree-ios/docs/todo.md`](../gantree-ios/docs/todo.md); this file is what makes
them *gantree2* apps. Checked boxes mean done in this repo.

Wire names were fixed in gantree on 2026-10-10 (see its
`docs/todo.md §0`): `CHAT_*`, `CHANNEL=chat`, `agents`,
`chat:<slug>:<sub>`. Nothing new here is written with the old ones.

## Needs gantree (the Worker)

- [x] Native Google sign-in landed 2026-10-11 in gantree2
      (`lib/door/native.ts`). Both apps read the contract (`agents`,
      403 `not_allowed` shows the sub).

## Needs a Mac

- Rename the Xcode target, scheme, and project from Helm to gantree
  via `gantree-ios/app/project.yml` + `xcodegen generate`.
- Re-check the CarPlay entitlement.
- Run `make -C gantree-ios ios`.

## 0. Repo

- [x] Copy the two apps into `gantree-android/` and `gantree-ios/`, no `.git`,
      no build output, no `.env` / `local.properties`.
- [x] `shared/theme/` generated from gantree.
- [ ] `git init`, remote `shotah/gantree2-apps`, first push. **Owner
      does this**; agents do not run git here.
- [x] Root `.gitignore` = union of the two apps' ignores, scoped per
      folder where the pattern is folder-specific. `shared/**` is
      *tracked* (it is generated, but committed so a clone builds).
- [x] Root `LICENSE` (both apps had the same MIT text; one copy kept).
- [x] Root `VERSION` (`v0.2.0`). One tag ships both. Per-app `VERSION`
      files go with the app pass.

## 1. CI — one workflow, two jobs

- [x] `.github/workflows/ci.yml`: `android` (ubuntu, JDK 21, Gradle
      cache, script tests, lint, unit tests, coverage) and `ios`
      (ubuntu, Swift 6.3.3, `swift test` + coverage) plus `ios-app`
      (macos, `make -C gantree-ios ios`). Path filters so a Swift-only
      change does not spin Gradle. Each app filter includes `shared/**`.
- [x] `release.yml` on `v*`: release APK + unsigned iOS archive as
      release assets. **Never fail the tag for a missing keystore,
      cert, or Play/App Store key.**
- [x] Coverage badge per app to `gh-pages` `badges/android.svg` and
      `badges/ios.svg`, same shields-style SVG gantree publishes;
      README badge row.
- [x] `theme-drift` job: run gantree's export against a pinned gantree
      ref and fail when `shared/` differs. Fails when someone edited
      the generated files or gantree moved a hex or the mark without
      re-exporting.

## 2. Wire rename (both apps, same commit)

Server side lands in gantree with `cranes` kept as an alias until
cutover, so these can ship before or after.

- [x] `/api/auth/me`: read `agents`; fall back to `cranes` while the
      alias exists (2026-10-11). gantree-android `AuthApi.kt` +
      `CabViewModel`; gantree-ios `Auth.swift`.
- [ ] Socket path stays `/ws/<slug>`; the slug is an **agent** slug.
      Rename variables, strings, and tests (`crane` → `agent`).
- [ ] Settings copy: "pendant Worker host" → "gantree origin";
      `PENDANT_MAILBOX_URL` / `PENDANT_BEARER` mentions → `CHAT_*`
      (gantree-android `CabSettings.kt` ~L135–184; gantree-ios `Look.swift` / settings
      view).
- [ ] Storage keys on the phone: `pendant.*` prefs → `gantree.*`, with
      a one-time migration read of the old key.
- [ ] `MAILBOX_SECRET` lab bypass: keep the mechanism, rename to what
      gantree's chat DO exposes. Decide with gantree §3.
- [ ] Tests: `Wire*Tests` fixtures regenerated from gantree's
      `test/chat/*` fixtures so both phones parse exactly what the
      Worker writes. One fixture folder in gantree, copied here by a
      script, not hand-edited.

## 3. Shared theme

- [ ] gantree-android: delete the fifteen `*Colors()` functions in
      `ui/CabPalette.kt`; `cabColors(id)` maps `GantreeThemes.of(id)`
      → `CabColors`. Add `shared/theme/GantreeThemes.kt` as a Gradle
      `srcDir` (no copy).
- [ ] gantree-ios: same for `Sources/Mailbox/Palette.swift`; add
      `shared/theme/GantreeThemes.swift` to the `Mailbox` target via a
      symlink or SwiftPM `path:` entry.
- [ ] `parseTheme` in both apps → `GantreeThemes.of(...)`. Delete the
      hard-coded id lists.
- [ ] Both apps' theme tests assert against `shared/theme/tokens.json`
      rather than literal hexes, so the test moves when the catalog
      moves.
- [ ] `followTheme` / room theme from the Worker (`gantree.roomTheme`)
      honored on the phone exactly as the PWA does; same event order.

## 4. Brand

Assets come from gantree `assets/` once its tree mark ships. The
generated set lives in `shared/brand/` (`node scripts/icons.ts --apps`
from the gantree checkout). CI `theme-drift` re-checks it. Copying
into each launcher is in-folder:
`gantree-android/scripts/launcher-icon.sh`,
`gantree-ios/scripts/app-icon.sh`.

- [ ] App name "gantree" on both launchers; bundle ids
      `com.gantree.cab` / `com.gantree.helm` stay (already correct).
- [ ] Adaptive icon (Android) and asset catalog (iOS) from the tree
      mark: foreground SVG → PNG set via a script in gantree
      `scripts/`, outputs copied here.
- [ ] Splash: canvas color from `boom.canvas`, mark centered. Same
      spec on both.
- [ ] `assets/banner.svg` and `assets/logo.svg` in each folder
      replaced; READMEs' first line rewritten without the old product
      copy.
- [ ] Android Auto / CarPlay card icon = the mark on `accent`.
- [ ] Replace hand-painted doc screenshots (gantree-android `DocsShot.kt`) with
      device captures once the rebrand lands — one capture script,
      outputs to `docs/shots/`.

## 5. Docs carried from the old repos

Rewrite in place under each app's `docs/`; drop the pre-gantree product
names; link to gantree docs by relative path to the sister
checkout only where a wire contract is defined there.

| Old | Action |
| --- | --- |
| `pendant_handoff.md` (both) | → `chat_handoff.md`; the mailbox contract now points at gantree `docs/architecture.md` chat section |
| `helm_parity.md` (gantree-android) | Keep; it is the two-app parity table — move to **this** `docs/parity.md` (next pass) |
| `sibling_phones.md` (both) | Merge into one `docs/sibling_phones.md` here |
| `screens.md` (both) | Keep per app; rebrand |
| `setup.md`, `sideload_to_*.md` | Keep per app; origin URL examples → gantree |
| `android_auto_setup.md`, `carplay_setup.md`, `carplay.md` | Keep per app |
| `fcm_design_and_todo.md`, `apns_design_and_todo.md` | Keep; push is still "not this version" on both |

Done this pass: `pendant_handoff.md` → `chat_handoff.md` in both apps;
lowercase wire tokens (`crane`, `MAILBOX_SECRET`, `gantry-cab`,
`gantry-helm`) swapped; cross-app links are relative paths now.
Still open: capitalised `Crane` / `Pendant` prose, and every link into
`github.com/shotah/gantry-pendant/blob/main/docs/{frontends,voice,design}.md`
is dead — gantree2 has no `docs/frontends.md` or `docs/voice.md`; point
those at gantree2 `docs/architecture.md` / `docs/channels.md` or drop them.
| `motion.md`, `design.md` | Merge into `docs/theming.md` here (motion + palette are shared rules) |
| `device_actions.md`, `draft_stream_handoff.md` | Keep in gantree-android until gantree-ios has parity, then move here |

## 6. Carried per-app backlog (summary — detail in each app's todo)

gantree-android: wrap credentials at rest (High) · `verification-metadata.xml` ·
release minify · ktlint · `:mailbox` JVM module · test the Android
side · "drop a pin" shortcut · tag slow tests.

gantree-ios: walk sibling inbound on a deployed origin · CarPlay conversation
screen (needs Apple entitlement) · Communication / Time Sensitive
Notifications capabilities · entitlements file · drive-test CarPlay.

## Not this version

- Play Store / App Store listings. Sideload is the product.
- Push (FCM/APNs). Both design docs stay; neither ships.
- A third app or a shared Kotlin Multiplatform core. Two thin clients
  reading one generated theme file is the whole "shared" story.
