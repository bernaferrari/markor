# Markor Compose Port (2026)

A modern, local-first, Markdown notes app inspired by the original Markor and rebuilt with Kotlin Multiplatform + Compose.

This project respects Markor’s core philosophy: plain files, no lock-in, fast editing, and offline-first workflows.
It also brings a major leap in UX, architecture, and cross-platform consistency.

## Why This Exists

Classic Markor is still excellent at what it does.
This port focuses on what a 2026-grade Markor experience can look like:

- cleaner architecture
- modern Material 3 UI
- smoother navigation and transitions
- richer note metadata and organization
- stronger multi-platform foundation (Android + iOS + JVM)

## What Stays True To Original Markor

- Local files first
- Markdown-first editing
- Privacy by default (no account required)
- Fast note-taking and folder-based organization
- Lightweight behavior over cloud-heavy complexity

## What Is Better In This Port

## Design Notes and Inspirations

This app may look similar to Google Keep in a few places, but most of that overlap was accidental:

- Similarities like shared transitions, no bottom navigation, and colorful note cards were coincidence during iteration.
- The inline title row at the top of the editor was intentionally inspired by Google Keep.
- The colored-note border treatment was intentionally inspired by Craft.

### Product and UX

- Reimagined notebook home (grid/list, pinned-first sorting, filters, better previews)
- Search-first interactions integrated into the top app bar flow
- Split create action:
  - one tap to create and open a note
  - secondary action for folder creation
- Visual parent-folder navigation in the browser (not only OS back)
- Better empty states, safe-inset handling, and edge-to-edge behavior

### Navigation and Motion

- Typed Navigation 3 screen model
- Settings in back stack with proper slide-in behavior
- Shared transitions from home cards to editor (title + container/background)
- Cleaner transitions with reduced bounce/jank patterns

### Editing Experience

- Improved Compose editor with autosave and undo/redo history
- Rich markdown preview mode
- Tap preview to return to edit mode quickly
- Smart Enter behavior for list/task workflows
- Slash command insertion menu
- Outline panel from headings
- Expressive formatting toolbar styling

### Organization and Metadata

- Favorites, archive, labels, and trash workflow
- Recent tracking
- Pinned notes on top
- Metadata indexing for preview/title/image references
- Better sort/filter controls for daily use

### Color and Theming

- Modern Material 3 theming
- Better dark/light behavior for note colors and markdown preview contrast
- Improved theme consistency across editor and home previews

### Image and Asset Pipeline

- Local image insertion (Android/JVM)
- Per-note asset folder management
- Image-aware note previews
- Asset manager sheet for cleanup/orphan handling
- Share/export flows including markdown + assets ZIP

## Platform Behavior

| Capability | Android | iOS | JVM |
|---|---|---|---|
| Storage mode | Private + Shared | Private only | Private + Shared |
| Storage chooser in onboarding | Yes | No | Yes |
| Storage mode toggle in Settings | Yes | No (private-only display) | Yes |
| Image picker | Supported | Not implemented yet | Supported |

Notes:
- iOS intentionally skips storage mode choice (private only).
- iOS Settings intentionally does not show shared/private toggle.

## Architecture (High Level)

- `shared/` contains shared domain/data/UI/navigation logic
- Compose UI in `commonMain`
- Platform source sets for Android/iOS/JVM specifics
- DataStore-backed app settings
- Koin DI across shared + platform modules
- Room KMP metadata storage

## Project Structure

- `app/` Android host app
- `shared/` KMP shared module
- `iosApp/` iOS host app project

## Build and Run

## Prerequisites

- JDK 17
- Android Studio (Android)
- Xcode 15+ (iOS)

## Android

```bash
./gradlew :app:assembleFlavorDefaultDebug
./gradlew :app:installFlavorDefaultDebug
```

## Shared Compile Checks

```bash
./gradlew :shared:compileCommonMainKotlinMetadata
./gradlew :shared:compileAndroidMain
./gradlew :shared:compileIosMainKotlinMetadata
```

## iOS

- Open `iosApp/iosApp.xcodeproj`
- Build and run in Xcode

## Known Gaps

- iOS image picker integration is still pending (PHPicker bridge not finished).
- Some legacy, format-specific classic Markor behaviors are intentionally not 1:1.

## Original Project

Original Markor: https://github.com/gsantner/markor

## License

See `LICENSE.txt`.
