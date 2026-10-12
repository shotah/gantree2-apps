# gantree2-apps

Two native apps, one repo. `gantree-android` is Kotlin, Compose, Car App Library, Gradle, applicationId `com.gantree.cab`. `gantree-ios` is Swift, SwiftUI, SwiftPM package `Sources/Mailbox` plus an Xcode app still named Helm, bundle id `com.gantree.helm`. Launcher name on both is gantree. Do not rename the applicationId or bundle id in a drive-by.

## Commands

```sh
make test
make test-scripts
make check
make android-test
make android-lint
make android-build
make android-apk
make ios-test
make ios-lint
make ios-build
```

`make ios-test` uses Docker locally. `make ios-build` needs Xcode.

`shared/theme` and `shared/brand` are generated from the gantree2 checkout (`npm run theme:export` and `node scripts/icons.ts --apps`). Do not hand-edit them.

## Wire

The Worker is the other repo, `shotah/gantree2`. Contract is that repo's `docs/access.md` and `docs/openapi.yaml`. Phone chain is `GET /api/auth/config`, `GET /api/auth/nonce`, `POST /api/auth/token`, `GET /api/auth/me`, then `GET /ws/{slug}?role=phone` with `Authorization: Bearer` the session JWE. Room media and voice on the Worker are `/api/chat/avatar`, `/api/chat/backdrop`, `/api/chat/theme`, `/api/chat/tts`. Do not invent routes. Do not copy paths from old cab/helm comments if they disagree with that contract.

Secrets stay out of git. `.env` and `local.properties` are local. Fixtures use placeholders.

Least change. A behavior change needs a test in the tree it touches.

Write-ups belong in `docs/` as plain Markdown.
