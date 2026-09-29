# Stratos Smart Wake 0.26-dev

Prototype Smart Wake alarm for **Huami Amazfit Stratos (A1619/A1609)**.

The first goal is not to claim medical sleep-stage detection. The app estimates a good wake moment inside a configurable pre-alarm window by combining:

- wrist movement from the accelerometer,
- heart-rate rise when a usable HR source exists,
- transition from a stable period toward more movement,
- confirmation across multiple 30-second epochs.

The hard alarm remains scheduled until the user presses **STOP** or **SNOOZE**, so an early Smart Wake decision cannot remove the final deadline alarm if the process crashes.

## Current 0.26 features

- Alarm time
- Smart Wake window: 10 / 20 / 30 / 45 min
- Wake sensitivity: Gentle / Normal / Aggressive
- Accelerometer motion scoring
- Standard Android `TYPE_HEART_RATE` support when exposed by firmware
- Huami `content://com.huami.watch.health.heartdata` fallback
- Explainable WakeScore (motion + HR + transition)
- Requires 2 of the last 3 epochs above threshold
- 90-second minimum confirmation period
- Hard fallback alarm at the requested time
- Progressive vibration intro (~10 / ~22 / ~30 s)
- Strong repeating vibration after the intro until STOP/SNOOZE
- 10-minute Snooze
- Immediate vibration test that never changes the real alarm schedule
- Scheduled +2 minute alarm test that never changes the real alarm schedule
- CSV logging to `/sdcard/StratosSmartWake/logs/`
- Sensor diagnostics screen
- Re-schedules after reboot

## Important vibration detail

Old Stratos firmware does not give us modern per-pulse amplitude control that can be relied on across ROMs. The prototype therefore makes the wake pattern feel progressively stronger by increasing **pulse length and density**, not by changing motor amplitude.

## Build

The project intentionally uses plain Android Java without AndroidX or third-party dependencies.

1. Open the project root in a recent Android Studio.
2. Install Android SDK 34 when prompted.
3. Build `app` (`Build > Build APK(s)`).
4. The resulting debug APK is normally under `app/build/outputs/apk/debug/`.

Runtime compatibility is kept at `minSdk 21`, `targetSdk 22`, for the old Stratos Android base.

## Install on Stratos

Enable ADB on the watch/ROM, connect it through the charging cradle, then:

```bash
adb devices
adb install -r app-debug.apk
```

Launch **Smart Wake** from the app list.

## First test sequence

Do not use the first night as the only alarm you depend on.

1. Open **SENZORI** and record what the watch reports.
2. Press **TEST VIBRACIJE ODMAH** while awake. Confirm STOP works.
3. Press **TEST ALARMA ZA 2 MIN** and verify the screen/vibration wakes from idle. This leaves the real alarm untouched.
4. Set a real test alarm 10-15 minutes ahead with a 10-minute Smart Wake window.
5. Confirm the hard alarm still fires if Smart Wake does not trigger.
6. Then run a sleep/night test alongside a second independent alarm.
7. Pull logs after the test and use them to calibrate thresholds.

## Pull logs

Windows:

```bat
collect-logs.bat
```

or manually:

```bash
adb pull /sdcard/StratosSmartWake/logs ./stratos-smartwake-logs
```

CSV columns:

- timestamp
- RMS movement in g
- movement peaks/min
- heart rate
- motion score
- HR score
- transition score
- final wake score
- learned HR baseline
- wake decision

Lines beginning with `#` are diagnostic events.

## Current WakeScore

When HR exists:

`WakeScore = 0.50 * motion + 0.30 * HR + 0.20 * transition`

When HR is unavailable:

`WakeScore = 0.72 * motion + 0.28 * transition`

Initial thresholds:

- Gentle: 75
- Normal: 68
- Aggressive: 60

These are deliberately provisional. Real Stratos data should replace guesses as soon as the first logs are collected.

## Next development step

After one or more real logs:

- calibrate movement RMS and peak thresholds,
- determine whether standard HR events are live on the installed ROM,
- inspect freshness/frequency of Huami HR provider data,
- reduce wakelock/sensor battery cost,
- add morning feedback (Too early / Good / Too late),
- add per-user adaptive thresholding.

## CI build

`.github/workflows/build-apk.yml` builds the debug APK on GitHub Actions with Java 17, Gradle 8.7 and Android SDK 34, then publishes `app-debug.apk` as a workflow artifact.

See `FIRST_TEST.md` for the exact first-device test procedure.
