#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

if [ -z "$JAVA_HOME" ]; then
    if [ -d "/Users/bonfales/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.7+6/Contents/Home" ]; then
        export JAVA_HOME="/Users/bonfales/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.7+6/Contents/Home"
    elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
        export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
    fi
fi

if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    if [ -d "/opt/homebrew/share/android-commandlinetools" ]; then
        export ANDROID_HOME="/opt/homebrew/share/android-commandlinetools"
        export ANDROID_SDK_ROOT="/opt/homebrew/share/android-commandlinetools"
    elif [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
        export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
    fi
fi

chmod +x "$DIR/gradlew"

TARGET_TASK="assembleDebug"
INSTALL_AFTER=false

if [ $# -gt 0 ]; then
    case "$1" in
        release)
            TARGET_TASK="assembleRelease"
            ;;
        test)
            TARGET_TASK="test"
            ;;
        clean)
            TARGET_TASK="clean assembleDebug"
            ;;
        install)
            TARGET_TASK="assembleDebug"
            INSTALL_AFTER=true
            ;;
        *)
            TARGET_TASK="$*"
            ;;
    esac
fi

echo "Building StandBy Clock with: ./gradlew $TARGET_TASK"
"$DIR/gradlew" $TARGET_TASK

if [ "$INSTALL_AFTER" = true ]; then
    APK_PATH="$DIR/app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo "Installing to connected device via adb..."
        adb install -r "$APK_PATH"
        echo "Launching app..."
        adb shell am start -n com.standby.clock/.MainActivity
    fi
elif [ -f "$DIR/app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "Build succeeded!"
    echo "Debug APK located at: app/build/outputs/apk/debug/app-debug.apk"
    echo "Install with: adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo "Or run: ./build.sh install"
fi
