# Developing Wordkeep

Wordkeep is a Kotlin Multiplatform port of [Markor](https://github.com/gsantner/markor). One Compose Multiplatform codebase runs on Android, iOS and the web (Wasm).

<p>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?logo=kotlin" alt="Kotlin 2.4.0"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.12.0--alpha02-orange.svg?logo=jetpackcompose" alt="Compose Multiplatform"></a>
  <a href="../LICENSE.txt"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"></a>
</p>

## Tech stack

- **UI:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Material 3)
- **Database:** [Room](https://developer.android.com/kotlin/multiplatform/room) (KMP), for note metadata such as colors, pins and labels
- **Dependency injection:** [Koin](https://insert-koin.io/)
- **Navigation:** [Navigation 3](https://developer.android.com/jetpack/compose/navigation)
- **Preferences:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (KMP)
- **Concurrency:** [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- **Serialization:** [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Images:** [Coil 3](https://coil-kt.github.io/coil/) (KMP)
- **Theming:** [Material Kolor](https://github.com/jordond/MaterialKolor)

## Project structure

- `shared/`: common UI (Compose), business logic and data layers (Room, DataStore).
- `app/`: the Android entry point, widgets and resources.
- `iosApp/`: the Xcode project and Swift entry point.
- `webApp/`: the Wasm browser entry point for the [web demo](https://markor-five.vercel.app/).
- `store-listing/`: Google Play screenshots and listing text (see below).
- `branding/`: the Wordkeep icon master (`wordkeep-icon.svg`) and its 512 px Play icon. The Android adaptive icon in `app/src/main/res/drawable/ic_launcher_*.xml` draws the same artwork.
- `metadata/`: legacy store metadata from Markor.

## Building

### Android

```bash
./gradlew :app:installFlavorDefaultDebug
```

### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Select a simulator or device.
3. Click **Run**.

Or build the framework from the command line:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### Web

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun   # local dev server
./gradlew :webApp:wasmJsBrowserDistribution     # production build
```

## Store screenshots

The Play screenshots are real Android captures composed with Screenshot Studio (`store-listing/studio/`, synced from the shared studio; do not edit it here).

- `app/src/storeCapture/` is a capture-only build type (`com.bernaferrari.remarkor.storecapture`). Its `StoreCaptureActivity` seeds a private notebook with fictional notes on first launch. Production builds never include it.
- `store-listing/capture-emulator.py` drives a dedicated emulator (never a personal phone) through each scene for phone, 7-inch and 10-inch tablets, with System UI demo mode, and writes `store-listing/<locale>/raw/<profile>/*.png` plus layout sidecars.
- `store-listing/theme.css`, `theme.js`, `gallery.json` and `<locale>/copy.json` define the artwork.

```bash
./gradlew :app:assembleFlavorDefaultStoreCapture
python3 store-listing/capture-emulator.py --serial emulator-5570      # --profile phone, --locale de-DE
node store-listing/studio/export.cjs --locale en-US
node store-listing/studio/server.cjs 8790                              # live editor
```

Only `en-US` images are committed; other locales keep their words (`copy.json`, `listing.json`) in git and regenerate their images from the scripts.

## Credits and license

- **Original project:** [Markor](https://github.com/gsantner/markor) by Gregor Santner.
- **License:** Apache License 2.0. See [LICENSE.txt](../LICENSE.txt).
