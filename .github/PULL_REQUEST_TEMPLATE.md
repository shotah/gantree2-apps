## What

## Why

## Test evidence

- [ ] `make test-scripts`
- [ ] `make android-test` if this touches `gantree-android`
- [ ] `make ios-test` if this touches `gantree-ios` (Docker locally; Swift on CI)
- [ ] `make android-lint` if this touches `gantree-android`
- [ ] `make ios-lint` if this touches `gantree-ios`

CI also runs the path-filtered jobs in `.github/workflows/ci.yml`.

## Docs

- [ ] `docs/todo.md` ticked if this closes a line

## Secrets

- [ ] No `.env`, no `local.properties`, no keystores, no tokens in the diff

## Shared theme and brand

- [ ] `shared/theme` and `shared/brand` were not hand-edited (they are generated from the gantree2 checkout)
