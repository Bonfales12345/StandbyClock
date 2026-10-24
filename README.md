# StandBy Clock for Android

An Apple StandBy-inspired clock and bedside display for Android devices, engineered with authentic iOS UIScrollView physics, Material You dynamic color adaptation, OLED pure black theme, ambient low-light detection, and Android Screensaver (`DreamService`) support.

---

## Build Commands (`build.sh`)

A dedicated executable script [`build.sh`](build.sh) is provided in the project root to build, test, and install the application:

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

## Manual ADB Commands

If you prefer installing and launching manually using Android Debug Bridge:

```bash
# Install the debug build
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the main application
adb shell am start -n com.standby.clock/.MainActivity

# Test Android Screensaver (Dream) mode
adb shell am start-service -n com.standby.clock/.services.StandByDreamService
```

---

## Key Architecture & Features

- **iOS UIScrollView Physics**:
  - Exponential velocity decay fling with the iOS standard `0.998` deceleration rate.
  - Boundary rubber-banding using the rational boundary formula $f(x) = \frac{x \cdot d}{x + d}$.
  - Critically damped spring return ($\zeta = 1.0f$, zero oscillation).
  - Catch-on-touch interruptibility (freezes on touch down, resumes drag without jumping).
  - Hardware-accelerated GPU `RenderNode` updates with zero measure/layout passes during scrolling.
- **Ambient Low-Light Detection**:
  - Automatically switches to Night Red mode when light falls below `4.0 lux`.
  - Automatically dims screen brightness to minimum visible level (`0.01f`).
  - Restores daytime colors and adaptive brightness when room light exceeds `8.0 lux`.
- **Privacy-First Weather**:
  - Uses only authorized device GPS / Network location providers.
  - Zero IP address geolocation queries.
  - Gracefully displays `No Weather Information` when location permissions are absent.
- **Forced Landscape Engine**:
  - Edge-to-edge canvas rotation ensures true horizontal presentation even if external utilities or the system lock the device to portrait.
