# gantree2-apps

[![android coverage](https://shotah.github.io/gantree2-apps/badges/android.svg)](https://github.com/shotah/gantree2-apps/actions/workflows/ci.yml)
[![ios coverage](https://shotah.github.io/gantree2-apps/badges/ios.svg)](https://github.com/shotah/gantree2-apps/actions/workflows/ci.yml)

The phones for [gantree](https://github.com/shotah/gantree2). Two
native apps, one repo, one theme source, one brand.

| Folder | What | Stack |
| --- | --- | --- |
| [`gantree-android/`](gantree-android/) | Android phone + Android Auto message cards | Kotlin, Compose, Car App Library, Gradle |
| [`gantree-ios/`](gantree-ios/) | iPhone + CarPlay communication notifications | Swift, SwiftUI, SwiftPM (`Sources/Mailbox`) + Xcode app target |
| [`shared/theme/`](shared/theme/) | Generated palette — `tokens.json`, `GantreeThemes.kt`, `GantreeThemes.swift` | Written by gantree's `npm run theme:export`. **Do not edit.** |
| [`shared/brand/`](shared/brand/) | Generated launcher art — Android mipmaps, iOS AppIcon PNGs | Written by gantree's `node scripts/icons.ts --apps`. **Do not edit.** |
| [`docs/`](docs/) | Backlog and the rules both apps follow | Markdown |

```text
gantree-android APK ─┐
gantree-ios IPA ─────┼─►  gantree Worker  (/api/auth/me · /ws/<agent-slug>)
web app ─────────────┘
```

Both apps are the same program in two languages: sign in with Google
against the gantree origin, read `/api/auth/me` for the agents you may
talk to, hold one WebSocket per agent, paint the thread, and surface
the agent's reply as a car-safe message card. Native chain:
`/api/auth/config` → `/api/auth/nonce` → Google → `/api/auth/token` →
`/api/auth/me` → `/ws/<slug>?role=phone`
([Phones (native sign-in)](../../docs/access.md#phones-native-sign-in)).

## State

The folders are `gantree-android/` and `gantree-ios/`. The wire, the
shared theme, and the shared brand are in the tree. What still needs a
Mac: rename the Xcode target, scheme, and project via
`gantree-ios/app/project.yml` and `xcodegen generate`, then
`make -C gantree-ios ios`. See [docs/todo.md](docs/todo.md).

## Shared theme

The fifteen themes live once, in gantree
`lib/theme/catalog.ts`. From that checkout:

```sh
npm run theme:export -- ../gantree2-apps/shared/theme --kotlin-package com.gantree.theme
```

That rewrites the three files under `shared/theme/`. Both apps read
them (see [docs/theming.md](docs/theming.md)); a color change in gantree
is one command away from both phones.

## Shared brand

From the same gantree checkout:

```sh
node scripts/icons.ts --apps
```

That writes `shared/brand/` (Android mipmaps and `play-store.png`, iOS
`AppIcon-<size>.png`). Each app copies those into its own resources:

- `gantree-android/scripts/launcher-icon.sh`
- `gantree-ios/scripts/app-icon.sh`

## Run

`make test` at this root runs both apps. `make help` lists the rest.

| | gantree-android | gantree-ios |
| --- | --- | --- |
| Unit tests | `make -C gantree-android test` | `make -C gantree-ios test` (SwiftPM; Docker locally, Swift on CI) |
| Debug build | `make -C gantree-android build` | `make -C gantree-ios ios` (Xcode) |
| Docs | [gantree-android/docs/setup.md](gantree-android/docs/setup.md) | [gantree-ios/docs/setup.md](gantree-ios/docs/setup.md) |

One `VERSION` at the root. `make release` bumps it (`BUMP=patch` by
default, or `minor` / `major`, or `TAG=v0.2.0`), commits that file,
tags `vX.Y.Z`, and moves the floating `latest` tag. The release
workflow builds the APK and the iOS archive from that one tag. The
apps do not get separate version numbers. Lefthook does not bump
`VERSION`. CI is [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

`make install-hooks` installs lefthook. Pre-commit runs both apps'
tests in parallel. Pre-push runs four jobs at once: Android (script
tests, then lint and unit tests in one Gradle build), iOS lint, iOS
tests, and the root script tests. `make gate` runs that set without
pushing. Install lefthook first:
<https://github.com/evilmartians/lefthook#installation>.
