@echo off
setlocal enabledelayedexpansion

set "DIR=%~dp0"
cd /d "%DIR%"

if "%JAVA_HOME%"=="" (
    if exist "%ProgramFiles%\Android\Android Studio\jbr" (
        set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
    ) else (
        for /d %%D in ("%USERPROFILE%\.gradle\jdks\*21*") do (
            set "JAVA_HOME=%%D"
        )
    )
)

if "%ANDROID_HOME%"=="" if "%ANDROID_SDK_ROOT%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
        set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
    )
)

where adb >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if defined ANDROID_HOME (
        set "PATH=%PATH%;%ANDROID_HOME%\platform-tools"
    )
)

set "TARGET_TASK=assembleDebug"
set "INSTALL_AFTER=false"

if not "%~1"=="" (
    if /i "%~1"=="release" (
        set "TARGET_TASK=assembleRelease"
    ) else if /i "%~1"=="test" (
        set "TARGET_TASK=test"
    ) else if /i "%~1"=="clean" (
        set "TARGET_TASK=clean assembleDebug"
    ) else if /i "%~1"=="install" (
        set "TARGET_TASK=assembleDebug"
        set "INSTALL_AFTER=true"
    ) else (
        set "TARGET_TASK=%*"
    )
)

echo Building StandBy Clock with: gradlew.bat %TARGET_TASK%
call "%DIR%gradlew.bat" %TARGET_TASK%
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

set "APK_PATH=%DIR%app\build\outputs\apk\debug\app-debug.apk"

if "%INSTALL_AFTER%"=="true" (
    if exist "%APK_PATH%" (
        echo Installing to connected device via adb...
        adb install -r "%APK_PATH%"
        echo Launching app...
        adb shell am start -n com.standby.clock/.MainActivity
    )
) else if exist "%APK_PATH%" (
    echo.
    echo Build succeeded!
    echo Debug APK located at: app\build\outputs\apk\debug\app-debug.apk
    echo Install with: adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo Or run: build.bat install
)

endlocal
