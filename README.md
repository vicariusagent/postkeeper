# Postkeeper

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://android-arsenal.com/api?level=26)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Postkeeper is an Android app for collecting links and saving image or video media from public Instagram and X posts. Posts are stored locally on the device. No account is required.

## Features

- Add a post link in the app or share a text link from another app.
- Preview saved posts in a local collection, then download available media.
- Save media to the device’s `Pictures/Postkeeper` or `Movies/Postkeeper` folder.
- Choose **System**, **Light**, or **Dark** appearance. System is the default, and the choice is remembered.
- Use a monochrome Material 3 interface with Noto Sans, Material Symbols, and square component shapes.

### Download support

Instagram and X frequently change their pages and may require sign-in or block automated access. Postkeeper reads public page metadata; it cannot guarantee that every post, story, or reel can be extracted. Direct image and video file URLs are also supported when the server returns a media file. Failed extraction or download attempts are shown in the app.

## Technology

- Kotlin and Jetpack Compose with Material 3
- AndroidX Lifecycle and Kotlin Coroutines
- Room for the local collection
- Hilt for dependency injection
- OkHttp and Jsoup for public page/media requests
- Coil for image previews
- Material Symbols and bundled Noto Sans

The Noto Sans font’s SIL Open Font License is included at `app/src/main/res/font/OFL.txt`.

Dependencies are declared in `gradle/libs.versions.toml` and `app/build.gradle.kts`. The app uses only the libraries required by the current implementation; it does not include the unused Retrofit API stub, navigation, adaptive layouts, WorkManager, ExoPlayer, or Glide dependencies.

## Build

### Requirements

- JDK 17
- Android SDK Platform 35
- Gradle 8.12 (provided by the Gradle wrapper)

Build a debug APK from the project root:

```bash
./gradlew assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. The GitHub Actions workflow builds this APK on pushes and pull requests to `main` and `master`, and on manual runs. The APK is uploaded as a workflow artifact.

## Use

1. Open Postkeeper and tap **Add link**, or share a post’s text link to the app.
2. When the public page exposes supported media metadata, the post appears in your collection.
3. Tap **Download** to save the media to the device.

## License and disclaimer

This project is licensed under the [MIT License](LICENSE). Postkeeper is for personal archival use. Respect creators’ rights and do not redistribute media without permission. The app is not affiliated with Instagram, Meta, X, or Twitter.
