# Changelog

All notable changes to this fork are documented in this file.

This is a fresh fork. Historical entries from upstream Markor were intentionally removed to keep this changelog focused on fork-specific development.

Upstream history:
- https://github.com/gsantner/markor/blob/master/CHANGELOG.md
- https://github.com/gsantner/markor/blob/master/NEWS.md

The format is based on Keep a Changelog, and this project uses Semantic Versioning.

## [Unreleased]

### Added
- No unreleased changes yet.

## [0.1.0] - 2026-02-23

### Added
- New fork baseline: Markor Compose Port.
- Kotlin Multiplatform project structure with shared `shared/` module.
- Compose-based UI foundation across Android, iOS, and JVM targets.
- Local-first notes workflow with Markdown-first editing.
- Modernized home/editor/settings flows and onboarding.
- Metadata-driven note organization (pinned, archive, labels, trash, recents).
- Asset-aware note support and Markdown + assets export pipeline.

### Changed
- Reworked architecture toward shared domain/data/UI/navigation layers.
- Updated app UX and design language for a modern Compose experience.

### Known Issues
- iOS image picker integration is pending.
- Some legacy, format-specific behaviors from classic Markor are not 1:1.
