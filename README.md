# Zyron Bedrock Client (Android)

Independent overlay client. It does **not** bundle Minecraft Bedrock and does **not** bypass Microsoft/Xbox authentication.

## What it does

1. Splash → native ARM64 preload (`libzyron_runtime.so` built from `app/src/main/cpp`).
2. Asks for `SYSTEM_ALERT_WINDOW`.
3. Starts a foreground overlay service.
4. Launches the user's installed `com.mojang.minecraftpe` if present.
5. Draws FPS/CPS HUD and the Zyron client menu on top of the running game.
6. Accepts `.mcpack` / `.mcworld` share targets with confirmation. Never silent-deletes worlds.

## Build an APK

This repository is an Android Studio project.

1. Install Android Studio (Koala+), Android SDK 35, NDK 26, CMake 3.22.
2. Open this folder.
3. Let Gradle sync. ABI is `arm64-v8a` only.
4. Build → Generate Signed Bundle / APK.

```
./gradlew :app:assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`.

## Architecture

- Kotlin: lifecycle, overlay, Bedrock launch, pack import, crash screen.
- C++/JNI: version, ABI, monotonic clock, FPS cap state. Built from source. Not a fake Minecraft engine.
- Bedrock remains the proprietary runtime the user already owns.

## Legal

Zyron is not affiliated with Mojang or Microsoft. Do not ship Mojang assets inside this APK.
