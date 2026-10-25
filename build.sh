#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

OS="$(uname -s)"
case "$OS" in
    Linux*)     MACHINE="Linux";;
    Darwin*)    MACHINE="Mac";;
    CYGWIN*|MINGW*|MSYS*) MACHINE="Windows";;
    *)          MACHINE="UNKNOWN:$OS";;
esac

if [ -z "$JAVA_HOME" ]; then
    GRADLE_JDK=$(find "$HOME/.gradle/jdks" -maxdepth 3 -name "*21*" -type d 2>/dev/null | head -n 1)
    if [ -n "$GRADLE_JDK" ]; then
        if [ -d "$GRADLE_JDK/Contents/Home" ]; then
            export JAVA_HOME="$GRADLE_JDK/Contents/Home"
        else
            export JAVA_HOME="$GRADLE_JDK"
        fi
    elif [ "$MACHINE" = "Mac" ] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
        export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
    elif [ "$MACHINE" = "Linux" ]; then
        for jpath in \
            "/usr/lib/jvm/java-21-openjdk" \
            "/usr/lib/jvm/java-21-openjdk-amd64" \
            "/usr/lib/jvm/jdk-21" \
            "/opt/android-studio/jbr" \
            "$HOME/.sdkman/candidates/java/current"; do
            if [ -d "$jpath" ]; then
                export JAVA_HOME="$jpath"
                break
            fi
        done
    elif [ "$MACHINE" = "Windows" ]; then
        for jpath in \
            "/c/Program Files/Android/Android Studio/jbr" \
            "/c/Program Files/Eclipse Adoptium/jdk-21"*; do
            if [ -d "$jpath" ]; then
                export JAVA_HOME="$jpath"
                break
            fi
        done
    fi

    if [ -z "$JAVA_HOME" ] && command -v java >/dev/null 2>&1; then
        JAVA_BIN="$(command -v java)"
        if command -v readlink >/dev/null 2>&1; then
            JAVA_BIN="$(readlink -f "$JAVA_BIN" 2>/dev/null || echo "$JAVA_BIN")"
        fi
        export JAVA_HOME="$(cd "$(dirname "$JAVA_BIN")/.." && pwd)"
    fi
fi

if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    SDK_CANDIDATES=(
        "$HOME/Library/Android/sdk"
        "$HOME/Android/Sdk"
        "/opt/homebrew/share/android-commandlinetools"
        "/usr/local/share/android-commandlinetools"
        "/opt/android-sdk"
        "/usr/lib/android-sdk"
    )
    if [ -n "$LOCALAPPDATA" ]; then
        SDK_CANDIDATES+=("$LOCALAPPDATA/Android/Sdk")
    fi
    if [ -n "$USERPROFILE" ]; then
        SDK_CANDIDATES+=("$USERPROFILE/AppData/Local/Android/Sdk")
    fi

    for sdk in "${SDK_CANDIDATES[@]}"; do
        if [ -d "$sdk" ]; then
            export ANDROID_HOME="$sdk"
            export ANDROID_SDK_ROOT="$sdk"
            break
        fi
    done
fi

if [ "$MACHINE" = "Windows" ]; then
    GRADLEW="$DIR/gradlew.bat"
    ADB_CMD="adb.exe"
else
    GRADLEW="$DIR/gradlew"
    ADB_CMD="adb"
    chmod +x "$GRADLEW" 2>/dev/null || true
fi

if ! command -v "$ADB_CMD" >/dev/null 2>&1 && [ -n "$ANDROID_HOME" ]; then
    export PATH="$PATH:$ANDROID_HOME/platform-tools"
fi

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

echo "Building StandBy Clock on $MACHINE with: $GRADLEW $TARGET_TASK"
"$GRADLEW" $TARGET_TASK

APK_PATH="$DIR/app/build/outputs/apk/debug/app-debug.apk"

if [ "$INSTALL_AFTER" = true ]; then
    if [ -f "$APK_PATH" ]; then
        echo "Installing to connected device via adb..."
        "$ADB_CMD" install -r "$APK_PATH"
        echo "Launching app..."
        "$ADB_CMD" shell am start -n com.standby.clock/.MainActivity
    fi
elif [ -f "$APK_PATH" ]; then
    echo ""
    echo "Build succeeded!"
    echo "Debug APK located at: app/build/outputs/apk/debug/app-debug.apk"
    echo "Install with: adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo "Or run: ./build.sh install"
fi
