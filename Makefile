# gantree2-apps — both phones.
# Usage: make <target>

BUMP        ?= patch
TAG         ?=
DRY_RUN     ?=
SKIP_PUSH   ?=
ALLOW_DIRTY ?=

.PHONY: help
help: ## Show available targets
	@echo
	@echo gantree2-apps targets:
	@echo "  make test              Both apps' tests"
	@echo "  make test-scripts      Root script tests (no SDK, no Swift)"
	@echo "  make check             Root script tests + both apps' check"
	@echo "  make android-test      make -C gantree-android test"
	@echo "  make android-lint      make -C gantree-android lint"
	@echo "  make android-coverage  make -C gantree-android coverage"
	@echo "  make android-build     Debug APK"
	@echo "  make android-apk       Release APK"
	@echo "  make ios-test          make -C gantree-ios test"
	@echo "  make ios-lint          make -C gantree-ios lint"
	@echo "  make ios-coverage      make -C gantree-ios coverage"
	@echo "  make ios-build         Unsigned Simulator build (needs Xcode)"
	@echo "  make theme             Print theme + brand export commands"
	@echo "  make gate              Pre-push set: Android, iOS lint, iOS tests, scripts"
	@echo "  make install-hooks     lefthook pre-commit (tests) and pre-push (lint+test)"
	@echo "  make version           Show VERSION + next tag (dry-run)"
	@echo "  make release           One VERSION, one tag, both apps"
	@echo "  make clean             Both apps' clean"
	@echo
	@echo "Release: make release BUMP=patch|minor|major   or   make release TAG=v0.2.0"
	@echo "One tag ships both apps. Lefthook does not bump VERSION."
	@echo "Local iOS tests use Docker; CI does not."
	@echo

.PHONY: test-scripts
test-scripts: ## Bash tests under test/scripts
	@fail=0; \
	for t in $(CURDIR)/test/scripts/*.test.sh; do \
	  echo "==> $$t"; \
	  bash "$$t" || fail=1; \
	done; \
	exit $$fail

.PHONY: test
test: ## Both apps' tests
	$(MAKE) -C gantree-android test
	$(MAKE) -C gantree-ios test

.PHONY: check
check: test-scripts ## Root scripts + both apps' check
	$(MAKE) -C gantree-android check
	$(MAKE) -C gantree-ios check

.PHONY: android-test
android-test: ## make -C gantree-android test
	$(MAKE) -C gantree-android test

.PHONY: android-lint
android-lint: ## make -C gantree-android lint
	$(MAKE) -C gantree-android lint

.PHONY: android-coverage
android-coverage: ## make -C gantree-android coverage
	$(MAKE) -C gantree-android coverage

.PHONY: android-build
android-build: ## Debug APK
	$(MAKE) -C gantree-android build

.PHONY: android-apk
android-apk: ## Release APK
	$(MAKE) -C gantree-android apk

.PHONY: ios-test
ios-test: ## make -C gantree-ios test
	$(MAKE) -C gantree-ios test

.PHONY: ios-lint
ios-lint: ## make -C gantree-ios lint
	$(MAKE) -C gantree-ios lint

.PHONY: ios-coverage
ios-coverage: ## make -C gantree-ios coverage
	$(MAKE) -C gantree-ios coverage

.PHONY: ios-build
ios-build: ## Unsigned Simulator build (needs Xcode)
	$(MAKE) -C gantree-ios ios

.PHONY: theme
theme: ## Print export commands; run them from the gantree checkout
	@echo "From the gantree checkout:"
	@echo "  npm run theme:export -- ../gantree2-apps/shared/theme --kotlin-package com.gantree.theme"
	@echo "  node scripts/icons.ts --apps"

.PHONY: gate
gate: ## Lint and test both apps in parallel (same set as pre-push)
	lefthook run pre-push

.PHONY: install-hooks
install-hooks: ## Install lefthook pre-commit (tests) and pre-push (lint + test)
	@top=$$(git rev-parse --show-toplevel 2>/dev/null); \
	here=$$(cd "$(dir $(abspath $(lastword $(MAKEFILE_LIST))))" && pwd); \
	if [ -z "$$top" ] || [ "$$top" != "$$here" ]; then \
	  echo "init this folder as its own git checkout first" >&2; \
	  echo "git root is $${top:-"(none)"}" >&2; \
	  exit 1; \
	fi
	cp scripts/pre-commit .git/hooks/pre-commit
	chmod +x .git/hooks/pre-commit
	cp scripts/pre-push .git/hooks/pre-push
	chmod +x .git/hooks/pre-push
	@echo "Installed .git/hooks/pre-commit and pre-push (lefthook)"
	@command -v lefthook >/dev/null || echo "lefthook is not on PATH; hooks fail until it is installed" >&2

.PHONY: version
version: ## Show VERSION file and next tag (dry-run)
	@echo "VERSION $$(tr -d '[:space:]' < VERSION)"
	@DRY_RUN=1 ./scripts/release.sh

.PHONY: release
release: ## Bump version + latest tags, update VERSION, push (BUMP= or TAG=)
	BUMP="$(BUMP)" TAG="$(TAG)" DRY_RUN="$(DRY_RUN)" SKIP_PUSH="$(SKIP_PUSH)" ALLOW_DIRTY="$(ALLOW_DIRTY)" ./scripts/release.sh

.PHONY: clean
clean: ## Remove both apps' build artifacts
	$(MAKE) -C gantree-android clean
	$(MAKE) -C gantree-ios clean

.DEFAULT_GOAL := help
