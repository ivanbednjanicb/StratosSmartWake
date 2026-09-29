# Local build

Use JDK 17, Android SDK platform 34 and Build Tools 34.0.0.
Set JAVA_HOME to the JDK and ANDROID_HOME to the SDK directory (or set
sdk.dir in an untracked local.properties). Accept SDK licenses with sdkmanager --licenses.

Windows: ` .\gradlew.bat :app:assembleDebug :app:lintDebug scoreSelfTest`

If Windows/JDK reports `Unable to establish loopback connection` from
UnixDomainSockets, use a shorter socket directory before invoking Gradle:
`$env:JAVA_TOOL_OPTIONS='"-Djdk.net.unixdomain.tmpdir='+$PWD.Path+'"'` (PowerShell).

Linux/macOS: `bash ./gradlew :app:assembleDebug :app:lintDebug scoreSelfTest`

Output: `app/build/outputs/apk/debug/app-debug.apk` (debug signed).
AGP 8.5.2 uses Gradle 8.7 and JDK 17; compileSdk 34 matches its supported SDK.
Java 8 lambdas are desugared by the Android build tools.

minSdk 21 (Android 5.0) supports the Android 5.1 Stratos platform and the
Material theme. targetSdk 22 intentionally retains its legacy permission and
background-service behavior. Confirm the actual watch SDK before installation.
The app contains no native libraries, so it does not require an ARM-only ABI.
Lint disables only ExpiredTargetSdkVersion because Google Play target policy
does not apply to this sideload build. Other lint errors still fail CI.
The build JVM version does not determine the watch's Java runtime requirement.

GitHub Actions builds on every push, pull request and manual dispatch and
uploads the debug APK. Local and CI debug keys can differ; updates require the
same signing key. This is a sideload prototype, not a modern Play Store build.

Compatibility: setExact(RTC_WAKEUP) and FLAG_UPDATE_CURRENT are supported on
API 21/22; legacy Vibrator.vibrate is used without VibrationEffect. BOOT_COMPLETED
reschedules enabled daily alarms after the app has been opened once. A foreground
service and bounded partial WAKE_LOCK cover monitoring and vibration; the receiver
holds a 10-second handoff lock. Force-stop prevents alarms until the app is reopened.
OEM process/power behavior and exact delivery must be tested on the actual watch.
No unguarded post-Android-5 API is required. Later Android versions are not validated.

CSV uses the legacy shared-storage path /sdcard/StratosSmartWake/logs with
WRITE_EXTERNAL_STORAGE. Huami provider errors and denied standard HR access
fall back to motion. Monitoring restart resets the 90-second confirmation period.
Reboot restores a future snooze or reschedules the daily alarm; tests are one-off
and are not restored. A snooze whose deadline passed during reboot resumes the
daily schedule. No promise of a powered-off alarm is made.
