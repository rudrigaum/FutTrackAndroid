# ⚽ FutTrack

The ultimate mobile application designed to manage amateur football matches, track top scorers, and maintain live tournament statistics.

## 📱 Features
* 📋 **Team Management:** Organize and view team rosters effortlessly.
* 🏆 **Gamified Leaderboard:** A reactive top-scorers board featuring customized gold, silver, and bronze podium highlights.
* 🔒 **Admin Access Toggle:** Secured configuration constraints preventing non-admin players from manipulating goal counts.
* ☁️ **Real-Time Cloud Sync:** Scaled persistence backing using Firebase Firestore to synchronize match data across devices.
* 💾 **Offline-First Storage:** Local caching capabilities using Room Database to ensure usability anywhere.

## 🛠️ Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Architecture:** Clean Architecture + Reactive MVVM
* **Dependency Injection:** Dagger Hilt
* **Concurrency & Streams:** Coroutines & Flow
* **Local Persistence:** Room Database
* **Remote Persistence:** Firebase Firestore

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone [https://github.com/YOUR-USERNAME/futtrack.git](https://github.com/YOUR-USERNAME/futtrack.git)

2. Open the project inside Android Studio.

3. Place your infrastructure's google-services.json (Firebase configuration) inside the app/ directory.

4. Sync the project with Gradle files and execute it on a physical device or emulator.