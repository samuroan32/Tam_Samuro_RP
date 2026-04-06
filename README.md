# TwinScale (Private 2-Person Size App)

TwinScale is a Kotlin + Jetpack Compose (Material 3) Android app for exactly **two users** in one private room.

## Tech Stack
- Kotlin
- Jetpack Compose + Material 3
- Firebase Realtime Database (no BigInteger writes)
- MVVM architecture

## Core Rules Implemented
1. Firebase never receives `BigInteger` objects.
2. Every size value in Firebase is stored as `String`.
3. `BigInteger` is only used internally for calculations.
4. Computed `BigInteger` properties use `@get:Exclude`.
5. Uses `com.google.firebase:firebase-database` (non-ktx).

## Firebase Data Layout
```
rooms/{roomCode}/
  participants/{userId}
  messages/{messageId}
  events/{eventId}
  meta/
```

## Firebase Setup
1. Create a Firebase project.
2. Enable **Realtime Database**.
3. Add Android app package: `com.example.twosize`.
4. Download `google-services.json` and place it in `app/google-services.json`.
5. If you use Google Services plugin, add it back in Gradle files.
   - This project intentionally avoids mandatory plugin wiring so the codebase compiles before Firebase secrets are added.
6. Set rules from `firebase-rules-example.json`.

## How Size Works
- User enters decimal + unit (mm/cm/m/km).
- App converts to canonical millimeters.
- Canonical mm is saved to Firebase as `String`.
- Grow/Shrink uses `SizeChangeCalculator` + mode (Gentle/Balanced/Extreme).

## Run
1. Open in Android Studio (JDK 17+).
2. Sync Gradle.
3. Connect emulator/device.
4. Run `app` configuration.

## Features Included
- Join room with validation
- 2-person room cap
- Grow/Shrink actions and event history
- Relationship ratio summary
- Chat + system messages
- Stats screen
- Copy room code
- Local user id persistence (DataStore)
- Online status
