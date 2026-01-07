# Disgaea 7 Evility Search (Android / Kotlin)

This is a Kotlin + Jetpack Compose Android port of the original React web project.

## What’s included
- Loads the same JSON datasets from `app/src/main/assets/data/`
- Category icons copied from the web app into `app/src/main/assets/images/`
- Search (name/description/source) + core filters (Unique/Generic/Player/Enemy/Base/DLC)
- Category filtering
- Simple “Builder” panel (add/remove evilities + share build string)

## Open in Android Studio
1. Unzip
2. Open the project folder **DisgaeaEvilitySearchAndroid** in Android Studio
3. Let Gradle sync and download dependencies
4. Run `app`

## Notes
- This project uses the Gradle wrapper (Gradle 8.7).
- Deep link handling is stubbed (see `AndroidManifest.xml` and `MainActivity`).
  If you want shareable URLs like the web app (`?build=1_2_3`), set your own domain
  and add a proper intent-filter + App Links verification.
