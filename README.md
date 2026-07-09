# Nepo - Modular Calculator & Converter

An Android modular application for mathematical calculation, scientific operations, and unit conversions, built with Kotlin and Jetpack Compose. Offers highly customizable styling with dynamic JSON-based themes, Google Fonts typography, local history, and an isolated parsing engine with absolute privacy.

[<img src="https://img.shields.io/badge/Download-Latest_Release-6A994E?style=for-the-badge&logo=android&logoColor=white" height="45">](https://github.com/Ixeken-Studios/nepo-app/releases/latest)

![Nepo Feature Graphic](/fastlane/metadata/android/en-US/images/featureGraphic.png)


## Key Features

### Calculation & Scientific Engines
- Basic calculations and advanced scientific operations (sin, cos, tan, ln, log, sqrt, cbrt, abs, exponentiation, pi, e).
- Responsive layout supporting both portrait grids and landscape grids with 15 scientific operators.
- Sanitized mathematical expression engine using a private encapsulated exp4j implementation.

### Dynamic Theme Engine
- Import and swap visual designs on the fly (e.g., "Glassy Premium", "Rustic Digital") loaded from JSON assets.
- Real backdrop blurs for glassmorphism styles implemented via Chris Banes' Haze library.
- Border radius, surface backgrounds, and button color mappings read from local assets.

### Dynamic Typography
- Real-time loading of 14 premium Google Fonts families (including Outfit, Josefin Sans, and others) or system default.
- Custom fonts propagate across all modular view boundaries.

### History Bottom Sheet
- Persistent list of chronologically executed equations.
- Grouped view of equations by date (Today, Yesterday, specific dates).
- Clean visual consistency with the main calculator screen layout and colors.

### Settings & Privacy
- **100% Local Privacy:** Calculation history and custom preferences stay exclusively on your device.
- Configurable sound and haptic tactile feedback on key presses.
- Check for updates linked directly to GitHub Releases with scrollable changelog dialogues.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.x |
| UI | Jetpack Compose, Material 3, Lucide Icons, Haze v1.7.2 |
| Architecture | Multi-Module Gradle, MVI (Model-View-Intent), Clean Architecture |
| Engine | exp4j (encapsulated privately in `:core:mathematics`) |
| Storage | SharedPreferences (JSON persistence for preferences/history) |
| Build | Kotlin DSL, Version Catalogs (`libs.versions.toml`), R8/Proguard optimization |

<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Android/android2.svg">&nbsp;&nbsp;
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Kotlin/kotlin2.svg">&nbsp;&nbsp;
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">&nbsp;&nbsp;
</p>

## Requirements

To build and run Nepo locally, make sure you have:
- **Android Studio Koala** (2024.1.1) or newer.
- **JDK 17** or newer (Android Studio comes with an embedded JetBrains Runtime JBR which is fully compatible).
- **Android SDK Platform 37** installed via Android Studio SDK Manager.

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Ixeken-Studios/nepo-app.git
cd nepo-app
```

### 2. Build the application

- **Debug Build (Recommended for testing):**
  - *Linux/macOS:* `./gradlew assembleDebug`
  - *Windows:* `.\gradlew.bat assembleDebug`

- **Release Build (Requires local signing configuration in `keystore.properties`):**
  - *Linux/macOS:* `./gradlew assembleRelease --no-configuration-cache`
  - *Windows:* `.\gradlew.bat assembleRelease --no-configuration-cache`

The compiled APKs will be located in: `app/build/outputs/apk/`

## Acknowledgements

- **[Lucide Icons](https://lucide.dev/)** - For the beautiful, clean, and modern open-source vector icon library.
- **[Haze](https://github.com/chrisbanes/haze)** - For providing native backdrop blur capabilities in Jetpack Compose.
- **[exp4j](https://www.objecthunter.net/exp4j/)** - For expression parsing and evaluation.

## License
<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/LicenceMIT/licencemit2.svg">
</p>
This project is licensed under the MIT License - see the LICENSE file for details.
