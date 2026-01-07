# Disgaea 7 Evility Search (Android)

An Android (Jetpack Compose) app for searching, filtering, and building “Evility” sets for **Disgaea 7**.

This project is a Kotlin/Android adaptation of the original evility-search project, redesigned for mobile usability:
- Filters live in a **bottom sheet** so the results list can use the whole screen
- Builder lives in its **own bottom sheet**
- Builds can be **shared** as a compact code and **imported** back into the app

---

## Features

### 🔎 Search
- Search evilities by:
  - Name
  - Description
  - Unlock text
  - Fixed/extra fields (when present)

### 🎛️ Filters (Bottom Sheet)
Toggles:
- Unique / Generic
- Player / Enemy
- Base Game / DLC

Categories:
- Filter by category (matches the original web app semantics)
- Category display names (when provided by `CategoryMap`)

### 🧱 Builder (Bottom Sheet)
- Enable “Builder mode”
- Add evilities from the results list
- Remove individual evilities
- Clear the build
- Share the build via Android share sheet
- Import a build from a share code

### 🔁 Share + Import Codes
The builder can output a share string like:

- `Disgaea 7 Evility build: 12_45_102`

The import flow accepts multiple formats:
- Full shared message (copy/paste):
  - `Disgaea 7 Evility build: 12_45_102`
- Raw underscore list:
  - `12_45_102`
- Comma/space-separated:
  - `12,45,102` or `12 45 102`
- Optional prefixed style:
  - `EV7:12,45,102`

---

## Screens

- **Main Screen**
  - Search bar
  - Full-height results list (mobile-friendly)
  - Top bar buttons for Filters + Builder

- **Filters Bottom Sheet**
  - Toggle filters + categories in a scrollable sheet

- **Builder Bottom Sheet**
  - Import / Clear / Share
  - Scrollable build list (bounded height to avoid Compose “infinite constraints” crashes)

---

## Project Structure

Key files:
- `MainActivity.kt` — hosts Compose UI, creates `EvilityViewModel`, handles Android share intent
- `EvilityViewModel.kt` — app state (`UiState`) + search/filter/build logic
- `EvilityRepository.kt` — loads evility data + category display name map
- `ui.kt` — Compose UI (results list + bottom sheets)
- `EvilityCard.kt` — UI component for each evility in the list
- `model.kt` — `Evility` + `CategoryMap` data models

---

## Requirements

- Android Studio (latest stable recommended)
- Kotlin + Jetpack Compose enabled
- Minimum Android version: depends on your project settings (check `minSdk` in Gradle)

---
