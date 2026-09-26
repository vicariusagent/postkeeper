# Postkeeper

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://android-arsenal.com/api?level=26)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Postkeeper** is a modern Android application designed to archive and save media from social media platforms. Simply share a post or story link from **Instagram** or **X (Twitter)** to Postkeeper, and it will automatically download the images and videos to your device.

Built with the latest Android technologies as of **September 2026**, Postkeeper leverages open-source libraries to provide a seamless, ad-free, and privacy-focused archiving experience.

## ✨ Features

- **📥 One-Tap Saving**: Share links directly from Instagram or X to the app to trigger downloads.
- **🖼️ Smart Media Handling**: Automatically detects and saves images as `.jpg/.png` and videos/stories as `.mp4`.
- **🌐 Platform Support**:
  - **Instagram**: Posts, Reels, and Stories.
  - **X (Twitter)**: Tweets with media and video posts.
- **📂 Organized Library**: View all your saved posts within the app with a clean, modern UI.
- **🔒 Privacy First**: No accounts, no tracking, and no ads. All data is stored locally on your device.
- **⚡ Modern Architecture**: Built with MVVM, Clean Architecture principles, and Jetpack components.

## 📸 Screenshots

*(Add screenshots here showing the Home screen, Downloading state, and Gallery view)*

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose & Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Networking**: OkHttp, Retrofit (for API interactions), Jsoup (for meta-tag parsing)
- **Image Loading**: Coil
- **Background Processing**: WorkManager
- **Async**: Kotlin Coroutines & Flow

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2024.1.1) or newer
- JDK 17+
- Android SDK 35 (Android 15)
- An Android device or emulator running Android 8.0 (API 26) or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/vicariusagent/postkeeper.git
   cd postkeeper
   ```

2. **Open in Android Studio**
   Open the project root in Android Studio and let Gradle sync complete.

3. **Build the Project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install the APK**
   The generated APK can be found at `app/build/outputs/apk/debug/app-debug.apk`. Install it on your device.

## 💡 How to Use

1. Open **Instagram** or **X**.
2. Find a post, reel, or story you want to save.
3. Tap the **Share** button.
4. Select **Postkeeper** from the share menu.
5. Postkeeper will automatically parse the link, extract the media, and download it to your device's `Downloads/Postkeeper` folder.
6. Open Postkeeper to view your saved collection.

## ⚙️ Configuration

No complex configuration is required. The app uses public web scraping techniques to extract media URLs. 

*Note: Due to the dynamic nature of social media platforms, extraction logic may require updates if platform structures change.*

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

Postkeeper is intended for personal archival purposes only. 
- Please respect the intellectual property rights of content creators.
- Do not use this app to redistribute content without permission.
- This app is not affiliated with Instagram, Meta, X, or Twitter.

## 🤝 Contributing

Contributions are welcome! If you find a bug or have a feature request, please open an issue or submit a pull request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📬 Contact

Project Link: [https://github.com/vicariusagent/postkeeper](https://github.com/vicariusagent/postkeeper)

---
*Built with ❤️ using Kotlin and Jetpack Compose*
