# Habits

> **Disclaimer:** This app is vibe coded with [Claude](https://claude.ai) and [ChatGPT](https://chatgpt.com).

Habit tracker and reward system built with **Android + Kotlin**.

The app is designed to help create daily structure with simple habit completion tracking and reward redemptions — without requiring accounts or cloud services.

## Features

- ✅ Create and track habits
- 📅 Calendar-based day tracking
- 🎁 Rewards + redemptions (spend points)
- 📊 Points balance (earned vs. spent)
- 📴 Offline storage (Room database)

## Tech Stack

- Kotlin
- Jetpack Compose (UI)
- Room (local database)
- Coroutines + Flow

## Getting Started

### Requirements
- Android Studio (latest stable)
- JDK 17 (Android Studio bundled is fine)

### Run
1. Clone the repo
2. Open in Android Studio
3. Press **Run** ▶️

## Project Structure (high level)

- `app/` — Android app module
- `app/data/` — database, DAOs, and repositories
- `app/ui/` — screens and composables
- `app/vm/` — ViewModels
- `app/util/` — utilities (date helpers, logger)

## License

Apache-2.0