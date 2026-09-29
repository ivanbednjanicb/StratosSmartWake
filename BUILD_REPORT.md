# StratosSmartWake 0.26-dev build report

Source imported from StratosSmartWake-0.26-source.zip into the repository root.

- Gradle wrapper 8.7, AGP 8.5.2, JDK 17, Android compile SDK 34.
- assembleDebug and lintDebug: BUILD SUCCESSFUL.
- WakeScore self-test: 5/5 including single spikes, motion fallback, 90-second
  boundary and nonconsecutive 2-of-3 confirmation.
- apksigner verifies v1 and v2 signing with minimum API 21.
- aapt confirms package hr.stratosx.smartwake, version 26 / 0.26-dev,
  minSdk 21, targetSdk 22, launcher and no native ABI dependencies.
- APK: app/build/outputs/apk/debug/app-debug.apk, 40,627 bytes.
- SHA256: 82e0dde0f4ff34855b8d25cfb4d515b3a82c04ed258be05d8b7f4ff87d631fec.

See validation/build-debug.txt and validation/apk-verification.txt for actual
command output. APK/build directories and downloaded toolchain are not committed.

Changes include foreground monitoring service, bounded wake locks, sticky restart
recovery, test-alarm isolation from real monitoring/scheduling, updating the alarm
screen when a real alarm replaces a test, future snooze recovery after boot,
guarded HR access and visible storage-failure diagnostics in logcat.

Device installation was not performed. Before the A1619 test, check firmware SDK,
HR sensor/provider availability and freshness, asleep-screen +2 minute delivery,
hard alarm timing, 22-second normal vibration progression, STOP/snooze, reboot,
process recovery, CSV write/pull, and overnight battery use. Foreground execution
reduces process risk but cannot guarantee behavior of Huami firmware.

No ROM, bootloader or system partitions were modified.

Toolchain compatibility references:
- https://developer.android.com/build/releases/agp-8-5-0-release-notes
- https://developer.android.com/reference/android/app/AlarmManager
- https://docs.github.com/en/actions/tutorials/store-and-share-data
