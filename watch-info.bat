@echo off
echo === DEVICE ===
adb devices
echo.
echo === ANDROID ===
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.fingerprint
echo.
echo === SENSORS ===
adb shell dumpsys sensorservice
pause
