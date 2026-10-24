### Quick Start
```bash
./build.sh
```
Compiles the debug application package and outputs the APK to:
`app/build/outputs/apk/debug/app-debug.apk`

---

### Command Reference

| Command | Action | Output / Behavior |
| :--- | :--- | :--- |
| `./build.sh` | Build Debug APK | Generates `app-debug.apk` |
| `./build.sh install` | Build & Install | Compiles and installs APK to connected device via `adb`, then launches the app |
| `./build.sh test` | Run Unit Tests | Executes the JVM test suite (`IOSScrollPhysicsTest`, `ClockLogicTest`) |
| `./build.sh clean` | Clean & Rebuild | Deletes build cache and rebuilds from scratch |
| `./build.sh release` | Build Release APK | Generates release build in `app/build/outputs/apk/release/` |
| `./build.sh <task>` | Custom Gradle Task | Passes arbitrary Gradle tasks directly (e.g. `./build.sh assembleDebug --dry-run`) |

---

## Prerequisites

- **Java Development Kit**: JDK 21
- **Android SDK**: Compile SDK 35, Minimum SDK 26 (Android 8.0 Oreo or higher)
- **Android Platform Tools**: `adb` command line tool (optional, for `./build.sh install`)

The script automatically detects `JAVA_HOME` and `ANDROID_HOME` from your environment, standard macOS directories (`/opt/homebrew`, `~/Library/Android/sdk`), and project properties (`gradle.properties`, `local.properties`).

---

Made with AI assistance.
