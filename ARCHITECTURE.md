# 🏛️ FutTrack Architecture

This project is built upon the principles of **Clean Architecture** combined with **Reactive MVVM / Unidirectional Data Flow (UDF)**. The main goal is to enforce a strict separation of concerns, ensuring highly testable, scalable, and framework-agnostic business logic.

## 📚 Architectural Layers

The codebase is modularized into three core layers:

### 1. Presentation Layer (UI)
Handles rendering the user interface and capturing user intents.
* **Jetpack Compose:** A modern, declarative UI framework used to build native screens seamlessly.
* **ViewModels:** Manage screen states via `StateFlow`. They capture user actions (intents) and forward them to the data pipelines, completely decoupling the UI state from configuration changes.

### 2. Domain Layer
The core of the application. This layer is entirely pure Kotlin and contains no dependencies on Android frameworks or external database libraries.
* **Models:** Pure Kotlin data classes representing core business concepts (e.g., `Player`, `Team`).
* **Repositories (Interfaces):** Core contracts defining data access behavior without implementing the actual data fetching logic.

### 3. Data Layer
Responsible for interacting with data sources such as local storage and remote networks.
* **Room Database (Local):** An offline-first SQL database implementation. It exposes data streams via asynchronous `Flow`.
* **Firebase Firestore (Remote):** A cloud-hosted NoSQL database that synchronizes player states across all clients in real-time using `callbackFlow`.
* **Mappers:** Extension functions that map data-specific models (`Entity` / `DTO`) into pure `Domain` entities before passing them up.

## ⚙️ Core Technology Stack & Patterns
* **Dependency Injection:** Powered by **Dagger Hilt** to ensure components depend strictly on abstractions rather than concrete implementations.
* **Asynchronous Pipelines:** Handled via **Kotlin Coroutines** and **Structured Concurrency**.
* **Single Source of Truth (SSOT):** The UI reacts dynamically to `Flow` emissions emitted straight from the persistent databases, ensuring consistency at all times.