# Amazfit Stratos A1619 / A1609

Enable the watch's existing ADB/debugging option and connect it over USB.
Use Android SDK platform-tools; these commands only install a user application.

```sh
adb devices
adb shell getprop ro.product.model
adb shell getprop ro.build.version.sdk
adb install app/build/outputs/apk/debug/app-debug.apk
# Update, retaining settings (requires matching signing key):
adb install -r app/build/outputs/apk/debug/app-debug.apk
# Uninstall (removes application settings):
adb uninstall hr.stratosx.smartwake
adb logcat -v time > stratos-logcat.txt
# Stop logcat with Ctrl+C. Copy CSV logs:
adb pull /sdcard/StratosSmartWake/logs ./stratos-csv
```

Confirm SDK >=21. Launch Smart Wake once after installation, then open sensor
diagnostics. Test the +2 min alarm with the screen asleep, STOP, snooze, a full
Smart Wake window, the hard deadline, and reboot recovery. Verify the real alarm
remains scheduled after a test. Check HR freshness, CSV output and battery use.
Default Normal vibration progresses for ~22 seconds, then repeats strongly;
Strong and Gentle retain the original ~10/~30 second alternatives.

An APK build verifies packaging and signing, not the watch firmware's sensor
access or alarm reliability. Keep an independent alarm during initial tests.
No ROM flashing, bootloader change, root or system-partition modification is needed.
