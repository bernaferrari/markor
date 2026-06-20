# Re-Markor (Compose + KMP + M3)

<p align="center">
  <img src="assets/more.png" alt="Re-Markor overview" width="980">
</p>

<p align="center">
  <strong>Local-first, Markdown-centric, Multiplatform notes.</strong><br>
  Built with Kotlin Multiplatform and Compose Multiplatform.
</p>

<p align="center">
  <a href="https://markor-five.vercel.app/"><strong>Try the Web Demo</strong></a>
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?logo=kotlin" alt="Kotlin 2.4.0"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.12.0--alpha02-orange.svg?logo=jetpackcompose" alt="Compose Multiplatform"></a>
  <a href="LICENSE.txt"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"></a>
  <a href="#"><img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Web-lightgrey.svg" alt="Platforms"></a>
</p>

---

Re-Markor is a modern, cross-platform port of the original [Markor](https://github.com/gsantner/markor) project. It preserves the core philosophy—plain text files, offline-first workflow, and no account lock-in—while leveraging **Kotlin Multiplatform (KMP)** to bring a unified experience to Android, iOS, and the web.

**Try it in your browser:** [markor-five.vercel.app](https://markor-five.vercel.app/)

## 🚀 Key Features

- 📝 **Markdown-First:** Fast editing with live preview and syntax highlighting.
- 🌍 **Multiplatform:** Shared business logic and UI across Android, iOS, JVM, and Web (Wasm).
- 🌐 **Web Demo:** Run Markor in the browser via Compose Multiplatform for Web.
- 📂 **Local-First:** Your data stays on your device in plain text files.
- 🏷️ **Smart Organization:** Pinned notes, archive, labels, trash, and recents.
- 🎨 **Modern UX:** A complete redesign using Material 3 and Compose Multiplatform.
- 🖼️ **Asset Aware:** Built-in support for images and attachments within your notes.

## 📸 Screenshots

![Comparison](assets/comparison.png)

## 🛠️ Tech Stack

This project is a showcase of modern Kotlin Multiplatform development:

- **UI:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Material 3)
- **Database:** [Room](https://developer.android.com/kotlin/multiplatform/room) (KMP)
- **Dependency Injection:** [Koin](https://insert-koin.io/)
- **Navigation:** [Navigation 3](https://developer.android.com/jetpack/compose/navigation)
- **Preferences:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (KMP)
- **Concurrency:** [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- **Serialization:** [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Images:** [Coil3](https://coil-kt.github.io/coil/) (KMP)
- **Theming:** [Material Kolor](https://github.com/jordond/MaterialKolor) (Dynamic Color)

## 🏗️ Project Structure

The project follows a standard KMP layout:

- `shared/`: The heart of the app. Contains common UI (Compose), business logic, and data layers (Room, DataStore).
- `app/`: Android-specific entry point and resources.
- `iosApp/`: iOS-specific Xcode project and Swift entry point.
- `webApp/`: Wasm browser entry point for the GitHub Pages demo.
- `metadata/`: App store metadata and screenshots.

## 🏁 Getting Started

### Prerequisites

- **JDK 17** or higher.
- **Android Studio** (Koala or newer) with the KMP plugin.
- **Xcode 15+** (for iOS development).
- **CocoaPods** (if applicable) or Swift Package Manager.

### Build & Run

#### Android
```bash
./gradlew :app:installFlavorDefaultDebug
```

#### iOS
1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Select a simulator or device.
3. Click **Run**.

Alternatively, via CLI:
```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

#### Web (local dev server)
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Production build:
```bash
./gradlew :webApp:wasmJsBrowserDistribution
```

#### Web deploy (Vercel)

Simulate the CI build locally before pushing:

```bash
./scripts/simulate-web-ci.sh
```

Deploy manually (after `vercel login` or with a token):

```bash
./scripts/verify-web-deploy.sh --ci
VERCEL_TOKEN=... VERCEL_ORG_ID=... VERCEL_PROJECT_ID=... ./scripts/deploy-web-vercel.sh
```

Pushes to `master` run `.github/workflows/deploy-web-vercel.yml`, which builds the wasm bundle and deploys to Vercel. Add these repository secrets:

| Secret | Where to find it |
|--------|------------------|
| `VERCEL_TOKEN` | [vercel.com/account/tokens](https://vercel.com/account/tokens) |
| `VERCEL_ORG_ID` | Vercel project → Settings → General |
| `VERCEL_PROJECT_ID` | Vercel project → Settings → General |

## 📜 Credits & License

- **Original Project:** [Markor](https://github.com/gsantner/markor) by Gregor Santner.
- **License:** Apache License 2.0. See [LICENSE.txt](LICENSE.txt) for details.

---
<p align="center">Made with ❤️ using Kotlin Multiplatform</p>
