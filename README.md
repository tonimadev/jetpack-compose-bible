# Bíblia Digital (Jetpack Compose)

[![Android CI](https://github.com/AnthoniIP/Biblie/actions/workflows/android.yml/badge.svg)](https://github.com/AnthoniIP/Biblie/actions/workflows/android.yml)

Simple and useful bible app for android, no ads, just reading.
Simple and beautiful design, optimized for OLED screens with a dark mode focus.

## Modern Infrastructure & Architecture

This project has been recently modernized to follow the latest Android development standards:

- **Language & Toolchain:** Kotlin 2.x with Java 21 and Gradle 9.x.
- **UI Framework:** 100% Jetpack Compose with Material Design.
- **Architecture:** **MVI (Model-View-Intent)** with **Composable Effects** (Monadic Computations).
- **Network Layer:** Declarative pipelines using the `Computation<C, R>` monad, replacing the traditional Repository pattern for cleaner effect management.
- **Build System:** Gradle Version Catalog (`libs.versions.toml`) for centralized dependency management.
- **Dependency Injection:** Hilt (Dagger) using **KSP** (Kotlin Symbol Processing).
- **Data Persistence:** Room Database with KSP.
- **Local Settings:** DataStore Preferences.
- **Quality Gates:**
  - **Detekt:** Static code analysis.
  - **Spotless (Ktlint):** Automated code formatting.
  - **MockK:** Comprehensive unit testing.
  - **GitHub Actions:** CI/CD pipeline for automated verification.

## Modularization: Feature-based & Bridge/Impl

The project has been refactored from a monolithic structure to a highly modular one. This architecture focuses on **build speed**, **separation of concerns**, and **parallel development**.

### Module Types

1.  **Core Modules (`:core:*`):** Provide shared functionality across the entire application.
    -   `:core:common`: Domain models, constants, and the `Computation` engine.
    -   `:core:database`: persistence implementation (Room & DataStore).
    -   `:core:network`: API configuration, Retrofit, and global Interceptors.
    -   `:core:ui`: Theme, components, and design system resources.
2.  **Feature Modules (`:feature:*`):** Encapsulate specific business domains.
    -   **Bridge (`:feature:[name]:bridge`):** Defines the **public API** of the feature (State, Intents, Navigation contracts). Lightweight and fast to compile.
    -   **Impl (`:feature:[name]:impl`):** Contains the internal implementation (ViewModels, Composables, UseCases).
3.  **App Module (`:app`):** The main entry point that wires all features together and provides the navigation host.

### Bridge/Impl Pattern Benefits
-   **Incremental Compilation:** Changes in a feature's UI or logic (`impl`) don't trigger recompilation of modules that depend on its contract (`bridge`).
-   **Strict Boundaries:** Features only communicate through their Bridges, preventing spaghetti code and circular dependencies.
-   **Scalability:** Independent modules allow the project to scale efficiently as more features are added.

## Architecture: MVI & Pure Reducers

The app follows a strict **MVI (Model-View-Intent)** pattern, recently refactored to use **Pure Reducers**. This ensures a predictable UI state, unidirectional data flow, and high testability by separating business logic from UI state management.

### The MVI Cycle

1.  **Intent:** User actions or system events (`BibleIntent`).
2.  **Side-Effects:** Asynchronous operations managed by the ViewModel (UseCases, TTS, IO).
3.  **Mutation:** Internal signals that describe *how* the state should change (`BibleMutation`).
4.  **Reducer:** A **pure function** `(State, Mutation) -> State` that produces a new immutable state.
5.  **State:** The single source of truth for the UI (`BibleState`), consumed via `StateFlow`.

### Core Components

- **`BibleState`:** Centralized immutable data class representing the entire UI state.
- **`BibleIntent`:** Sealed class representing user intentions (e.g., `LoadBooks`, `NextChapter`).
- **`BibleMutation`:** Internal sealed class that bridges the gap between side-effects and state updates.
- **`BibleReducer`:** A pure `object` that contains the synchronous logic for updating the state using `.copy()`.
- **`StateContainer`:** A delegate that provides a standard `updateState` mechanism, ensuring all state changes are processed through the reducer without forcing class inheritance.

### Benefits of Pure Reducers
- **Predictability:** Given the same state and mutation, the output state is always identical.
- **Testability:** The Reducer can be unit-tested without mocks, Coroutines, or Android dependencies.
- **Separation of Concerns:** The ViewModel handles "the how" (side-effects/coroutines), while the Reducer handles "the what" (state transformation).
- **Immutability:** Guarantees that the UI only updates when the state actually changes, preventing side-effect bugs.
- **Composition over Inheritance:** State management is plugged into ViewModels via Kotlin Delegation (`by`), keeping them lean and flexible.

## Composable Effects & Monadic Computations

The project implements a functional approach to networking and side-effects, moving away from the traditional Repository pattern towards **Monadic Computations**.

### Core Concepts

1.  **`Computation<C, R>` Monad:** Encapsulates a function `suspend (C) -> R`. This separates the **description** of an effect (what to do) from its **execution** (when and how to do it). These effects are organized in **Hilt-managed classes**, combining Dependency Injection with functional purity.
    *   `C`: The **Context** or **Capability** required (e.g., `ChurchRoomApi`, `PreferencesDataStore`).
    *   `R`: The **Result** of the computation.
2.  **Typed Capabilities:** APIs and data sources are modeled as capabilities (`Get`, `Post`, `Persistence`, `Database`). These are registered in a `CapabilityRegistry`, allowing for clean decoupling and easy mocking.
3.  **Functional Error Handling:** Uses the `Either<Failure, T>` type to manage errors. This ensures:
    *   **Fail-fast behavior:** Pipelines stop automatically on the first error.
    *   **Exhaustive checking:** The UI layer is forced to handle both Success and Failure cases.
4.  **Declarative Pipelines:** Instead of imperative logic, operations are composed using `map`, `flatMap`, and `flatMapResult`.

### Example: Declarative Pipeline

```kotlin
// Description of the effect (Lazy)
fun getBooks(): Computation<CapabilityRegistry, Either<Failure, List<Book>>> =
    Computation { registry ->
        val dao = registry.get(ChurchDao::class.java)
        val cached = dao.getAllBooks()

        if (cached.isNotEmpty()) {
            Either.Success(cached.map { it.toDomain() })
        } else {
            // Chain network call and database caching...
        }
    }

// Execution in the ViewModel (Runtime)
val result = getBooks()
    .switchContext(Dispatchers.IO)
    .runInContext(registry)
```

### Benefits
- **Testability:** Effects are values that can be tested by running them in a controlled context. Classes are Hilt-managed, allowing for easy mocking of entire effect factories.
- **Immutability:** Effects are immutable descriptions, preventing hidden side-effects.
- **Composability:** Complex dependencies are easily chained in a single readable pipeline.
- **DI Integration:** Leverages Hilt for lifecycle and dependency management without compromising functional purity.

## Features

- Offline reading (Room DB cache).
- Text-to-Speech support.
- Custom font size management.
- **Adaptive Layout:** 2-column layout for tablets and foldable devices.
- No ads, simple and clean interface.

## Screenshots

<p align="center">
  <img src="https://github.com/tonimadev/jetpack-compose-bible/blob/master/screenshots/Screenshot_20260810_020319.png" alt="List Books" width="250"/>
  <img src="https://github.com/tonimadev/jetpack-compose-bible/blob/master/screenshots/Screenshot_20260810_020333.png" alt="List Chapters" width="250"/>
  <img src="https://github.com/tonimadev/jetpack-compose-bible/blob/master/screenshots/Screenshot_20260810_020342.png" alt="Reading" width="250"/>
</p>
