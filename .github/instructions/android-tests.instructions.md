---
applyTo: "gantree-android/**/*Test.kt"
---

# Android tests

JVM unit tests under `app/src/test`. Package mirrors main (`com.gantree.cab`). Shell tests under `gantree-android/test/scripts` via `make test-scripts`. No network. No Android SDK required for the shell tests. Coverage gate is 70% on the mailbox helpers (`make android-coverage`).
