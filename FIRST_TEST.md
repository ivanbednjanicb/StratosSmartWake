# First Stratos A1619 test

## 1. Build and install

Build `app-debug.apk` in Android Studio or with the included GitHub Actions workflow.

Connect the watch through the charging cradle with ADB enabled:

```bash
adb devices
adb install -r app-debug.apk
adb shell monkey -p hr.stratosx.smartwake 1
```

## 2. Hardware diagnostics

Open **SENZORI** and note/save the complete list. Important items:

- ACCELEROMETER
- HEART_RATE, if exposed
- PRESSURE / GYROSCOPE / MAGNETOMETER (informational for later versions)
- `Huami provider` result

## 3. Safe alarm tests

1. Press **TEST VIBRACIJE ODMAH**. STOP must stop it immediately.
2. Press **TEST ALARMA ZA 2 MIN**. Turn the screen off and leave the watch alone.
3. Confirm that the screen wakes and vibration starts after ~2 minutes.
4. This test does **not** cancel or move the real alarm schedule.

## 4. Short Smart Wake test

Set the real alarm 12–15 minutes ahead and choose a 10-minute window.

- Stay mostly still for the first few minutes.
- Then deliberately move the wrist and raise HR slightly by standing/walking.
- Smart Wake should require sustained evidence, not one isolated movement.
- If it never triggers early, the exact hard alarm must still fire.

## 5. Pull logs

```bash
adb pull /sdcard/StratosSmartWake/logs ./stratos-smartwake-logs
```

Send back the newest CSV plus the **SENZORI** output. Those two items are enough to calibrate the next thresholds to the actual A1619 sensor behavior.
