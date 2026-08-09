# Bíblia Digital (Jetpack Compose)

[![Android CI](https://github.com/AnthoniIP/Biblie/actions/workflows/android.yml/badge.svg)](https://github.com/AnthoniIP/Biblie/actions/workflows/android.yml)

Simple and useful bible app for android, no ads, just reading.
Simple and beautiful design, optimized for OLED screens with a dark mode focus.

## Modern Infrastructure & Architecture

This project has been recently modernized to follow the latest Android development standards:

- **Language & Toolchain:** Kotlin 2.x with Java 21 and Gradle 9.x.
- **UI Framework:** 100% Jetpack Compose with Material Design.
- **Architecture:** **MVI (Model-View-Intent)** with Clean Architecture.
- **Build System:** Gradle Version Catalog (`libs.versions.toml`) for centralized dependency management.
- **Dependency Injection:** Hilt (Dagger) using **KSP** (Kotlin Symbol Processing).
- **Data Persistence:** Room Database with KSP.
- **Local Settings:** DataStore Preferences.
- **Quality Gates:**
  - **Detekt:** Static code analysis.
  - **Spotless (Ktlint):** Automated code formatting.
  - **MockK:** Comprehensive unit testing.
  - **GitHub Actions:** CI/CD pipeline for automated verification.

## Architecture: MVI

The app follows the MVI pattern to ensure a predictable state and unidirectional data flow:

- **State:** Centralized immutable UI State (`BibleState`).
- **Intent:** User actions or system events expressed as sealed classes (`BibleIntent`).
- **Event:** One-time UI events like showing errors or navigation (`BibleEvent`).

## Features

- Offline reading (Room DB cache).
- Text-to-Speech support.
- Custom font size management.
- **Adaptive Layout:** 2-column layout for tablets and foldable devices.
- No ads, simple and clean interface.

## Screenshots

![List Books](https://github.com/AnthoniIP/Biblie/blob/develop/screenshots/Screenshot_1658978743_google-pixel4-clearlywhite-portrait.png)
![List Chapters](https://github.com/AnthoniIP/Biblie/blob/develop/screenshots/Screenshot_1658978778_google-pixel4-clearlywhite-portrait.png)
![Reading](https://github.com/AnthoniIP/Biblie/blob/develop/screenshots/Screenshot_1658978754_google-pixel4-clearlywhite-portrait.png)
