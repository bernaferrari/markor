# Developing Wordkeep

A Kotlin Multiplatform port of [Markor](https://github.com/gsantner/markor): one Compose Multiplatform UI in `shared/` for Android, iOS and the web (Wasm). `app/` is the Android app.

[README](../README.md) · [Store listing](../store-listing/README.md) · [Web demo](https://markor-five.vercel.app/)

## Quick start

```bash
git clone https://github.com/bernaferrari/markor.git
cd markor
./gradlew :app:installFlavorDefaultDebug
```

## What you need

| Tool | Version | Notes |
| --- | --- | --- |
| JDK | 17 or newer | Bytecode targets 17; CI uses Temurin 21 |
| Gradle | 9.8.1 | Through `./gradlew` |
| Android SDK | Platform 37 | `minSdk` 26 |
| Xcode | Any with iOS 15 SDK or newer | iOS only; deployment target 15.0 |
| Node.js | Optional | Gradle downloads its own for the web build; needed only for Screenshot Studio |

## Run it

| Target | Command |
| --- | --- |
| Android | `./gradlew :app:installFlavorDefaultDebug` |
| iOS | Open `iosApp/iosApp.xcodeproj` in Xcode and press Run (it calls `:shared:embedAndSignAppleFrameworkForXcode`) |
| Web, dev server | `./gradlew :webApp:wasmJsBrowserDevelopmentRun` |
| Web, production build | `./scripts/verify-web-deploy.sh --serve` (builds, checks the dist and serves it) |

## Where things live

| Path | What's there |
| --- | --- |
| `shared/` | All UI, data and logic. `commonMain` plus `androidMain`, `iosMain`, `wasmJsMain`, `jvmMain` |
| `app/` | Android entry point and Glance widgets; flavors `flavorDefault`, `flavorGplay`, `flavorAtest` |
| `app/src/storeCapture/` | Screenshot-only build type that seeds fictional notes |
| `iosApp/` | Xcode project and Swift entry point |
| `webApp/` | Wasm browser entry point, deployed to Vercel |
| `scripts/` | Web deploy and verify scripts, Material Symbols generator |
| `store-listing/` | Play screenshots, listing text and icon layers (`brand/`) |
| `metadata/` | Legacy Markor changelogs and feature graphic |

| Concern | Library |
| --- | --- |
| UI | Compose Multiplatform, Material 3, Material Kolor |
| Storage | Room 3 (KMP) for note metadata, DataStore for preferences |
| DI and navigation | Koin, Navigation 3 |
| Other | Coroutines, Serialization, Coil 3, Glance |

## Before you push

```bash
./gradlew :shared:wasmJsBrowserTest :webApp:wasmJsBrowserDistribution
./scripts/verify-web-deploy.sh --skip-build
./gradlew :shared:allTests :app:testFlavorDefaultDebugUnitTest
```

The only CI is [deploy-web-vercel.yml](../.github/workflows/deploy-web-vercel.yml): it runs the first two lines on every push and pull request, then deploys `main` to production and same-repo PRs to a preview. Nothing in CI builds Android or iOS or runs their tests, so run the third line yourself (on a Mac it includes the iOS simulator tests).

## Store screenshots and icon

[Screenshot Studio](../store-listing/studio/) lives in `store-listing/studio/`. It is synced from `~/Documents/screenshot-studio`, so never edit it here.

- `storeCapture` is a debug-based build type (`com.bernaferrari.remarkor.storecapture`). Its `StoreCaptureActivity` seeds a private notebook with fictional notes. Build it with `./gradlew :app:assembleFlavorDefaultStoreCapture`.
- `node store-listing/studio/recapture.cjs` builds that APK, captures every size on the capture emulator (`CAPTURE_SERIAL`, default `emulator-5578`, never a phone) and exports. Add a locale (`en-US`), `--profile phone`, `--no-build` or `--no-capture`. The scene tour it runs is `store-listing/capture-emulator.py`.
- Export only: `PLAYWRIGHT_MODULE=… node store-listing/studio/export.cjs --locale en-US`. Live editor: `node store-listing/studio/server.cjs`.
- Icons are SVG layers in `store-listing/brand/` (main), `brand/todo`, `quicknote` and `linkbox` (widget variants) and `brand/atest` (the `flavorAtest` icon), listed in `brand/icon.json`. Regenerate every target with `PLAYWRIGHT_MODULE=… node store-listing/studio/icon.cjs`. Don't edit the generated `res/` icons by hand.

## Good to know

- The Gradle project is still named `Markor` and the package is `com.bernaferrari.remarkor`. Don't rename either: the package is the installed app id.
- `flavorAtest` installs beside the normal app (`com.bernaferrari.remarkor_test`) with a date-based version code, so you can test without touching your real notes.
- Release builds read optional local signing properties; credentials and keystores stay outside Git. See Google Play release below.
- Notes are plain files in app storage or a shared folder. Test with throwaway notes, and capture only on the disposable emulator.
- Credits: a port of [Markor](https://github.com/gsantner/markor) by Gregor Santner. Licensed under Apache 2.0 ([LICENSE](../LICENSE)).

## Google Play release

See [PLAY_STORE.md](PLAY_STORE.md) for listing preparation and declarations. Release signing is optional for local compilation and is loaded from an ignored root `signing.properties`, or the file named by `WORDKEEP_SIGNING_PROPERTIES`. A signed release requires `storeFile`, `storePassword`, `keyAlias` and `keyPassword`; keep this file and the upload keystore outside Git.

```sh
WORDKEEP_SIGNING_PROPERTIES="$HOME/.local/share/wordkeep/release-signing/signing.properties" ./gradlew :app:bundleFlavorGplayRelease
```

The AAB is written to `app/build/outputs/bundle/flavorGplayRelease/`. Back up the upload keystore and credentials securely. The Play signing key and upload key are separate when using Google Play App Signing.
