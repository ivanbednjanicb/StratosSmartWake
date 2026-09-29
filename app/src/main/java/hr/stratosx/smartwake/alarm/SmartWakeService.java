package hr.stratosx.smartwake.alarm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import hr.stratosx.smartwake.SmartWakeSettings;
import hr.stratosx.smartwake.score.EpochMetrics;
import hr.stratosx.smartwake.score.WakeScoreEngine;
import hr.stratosx.smartwake.score.WakeScoreResult;
import hr.stratosx.smartwake.sensors.HeartRateRepository;
import hr.stratosx.smartwake.sensors.MotionCollector;
import hr.stratosx.smartwake.storage.CsvLogger;
import hr.stratosx.smartwake.ui.AlarmActivity;

public final class SmartWakeService extends Service {
    public static final String ACTION_START_MONITORING = "hr.stratosx.smartwake.START_MONITORING";
    public static final String ACTION_FORCE_WAKE = "hr.stratosx.smartwake.FORCE_WAKE";
    public static final String ACTION_STOP = "hr.stratosx.smartwake.STOP";
    public static final String ACTION_SNOOZE = "hr.stratosx.smartwake.SNOOZE";
    public static final String ACTION_TEST = "hr.stratosx.smartwake.TEST";
    public static final String EXTRA_SMART = "smart";
    public static final String EXTRA_TEST = "test";

    private static final long EPOCH_MS = 30_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MotionCollector motion;
    private HeartRateRepository heartRate;
    private WakeScoreEngine engine;
    private CsvLogger logger;
    private PowerManager.WakeLock wakeLock;
    private VibrationController vibration;
    private SmartWakeSettings settings;
    private boolean monitoring;
    private boolean alarmActive;
    private boolean activeTest;

    private final Runnable epochTask = new Runnable() {
        @Override public void run() {
            if (!monitoring) return;
            long now = System.currentTimeMillis();
            MotionCollector.Snapshot ms = motion.snapshotAndReset(now);
            Integer bpm = heartRate.getBestBpm(now);
            EpochMetrics metrics = new EpochMetrics(now, ms.rmsG, ms.peaksPerMinute, bpm);
            WakeScoreResult result = engine.update(metrics);
            logger.log(metrics, result);
            logger.event("HR source=" + heartRate.getLastSource() + " samples=" + ms.samples);

            if (result.shouldWake) {
                logger.event("SMART_WAKE score=" + result.wakeScore);
                triggerWake(true, false);
            } else {
                handler.postDelayed(this, EPOCH_MS);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        settings = new SmartWakeSettings(this);
        motion = new MotionCollector(this);
        heartRate = new HeartRateRepository(this);
        vibration = new VibrationController(this);
        startForeground(26, new android.app.Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Stratos Smart Wake")
                .setContentText("Monitoring / alarm service")
                .setOngoing(true).build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (action == null) {
            // A sticky restart must resume work rather than leave an idle service.
            String saved = getSharedPreferences("service_state", MODE_PRIVATE).getString("mode", "");
            if ("alarm".equals(saved)) triggerWake(false, false);
            else if ("test".equals(saved)) triggerWake(false, true);
            else if ("monitor".equals(saved) && settings.isEnabled()) startMonitoring();
            else stopSelf();
        }
        if (ACTION_START_MONITORING.equals(action)) {
            startMonitoring();
        } else if (ACTION_FORCE_WAKE.equals(action)) {
            triggerWake(false, false);
        } else if (ACTION_TEST.equals(action)) {
            triggerWake(false, true);
        } else if (ACTION_STOP.equals(action)) {
            stopAlarmAndScheduleNext(intent != null && intent.getBooleanExtra(EXTRA_TEST, false));
        } else if (ACTION_SNOOZE.equals(action)) {
            snooze(intent != null && intent.getBooleanExtra(EXTRA_TEST, false));
        }
        return START_STICKY;
    }

    private void startMonitoring() {
        if (monitoring || alarmActive) return;
        long now = System.currentTimeMillis();
        monitoring = true;
        saveMode("monitor");
        engine = new WakeScoreEngine(settings.getSensitivity().threshold, now);
        logger = new CsvLogger(now);
        logger.open();
        logger.event("MONITOR_START threshold=" + settings.getSensitivity().threshold +
                " window_min=" + settings.getWindowMinutes() +
                " accel=" + motion.isAvailable() +
                " standard_hr=" + heartRate.hasStandardSensor());

        acquireWakeLock();
        motion.start();
        heartRate.start();
        handler.removeCallbacks(epochTask);
        handler.postDelayed(epochTask, EPOCH_MS);
    }

    private void stopMonitoring() {
        if (!monitoring) return;
        monitoring = false;
        handler.removeCallbacks(epochTask);
        motion.stop();
        heartRate.stop();
        if (logger != null) {
            logger.event("MONITOR_STOP");
            logger.close();
            logger = null;
        }
        releaseWakeLock();
    }

    private void triggerWake(boolean smart, boolean test) {
        if (alarmActive && (!activeTest || test)) return;
        if (!test) stopMonitoring();
        alarmActive = true;
        activeTest = test;
        saveMode(test ? "test" : "alarm");
        acquireWakeLock();
        // Keep the hard alarm armed until STOP/SNOOZE as a crash-safe fallback.
        vibration.start(settings.getVibrationProfile());

        Intent activity = new Intent(this, AlarmActivity.class);
        activity.putExtra(EXTRA_SMART, smart);
        activity.putExtra(EXTRA_TEST, test);
        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activity);
    }

    private void stopAlarmAndScheduleNext(boolean test) {
        if (test != activeTest) return;
        vibration.stop();
        alarmActive = false;
        if (!monitoring) releaseWakeLock();
        if (test) {
            // A test must never alter or skip the real alarm schedule.
            AlarmScheduler.cancelTest(this);
        } else {
            AlarmScheduler.scheduleFollowingDay(this);
        }
        saveMode(monitoring ? "monitor" : "");
        if (!monitoring) stopSelf();
    }

    private void snooze(boolean test) {
        if (test) {
            stopAlarmAndScheduleNext(true);
            return;
        }
        vibration.stop();
        alarmActive = false;
        releaseWakeLock();
        if (test) {
            // Keep test behavior harmless: dismiss it and restore the real schedule.
            AlarmScheduler.cancelTest(this);
            AlarmScheduler.scheduleNext(this);
        } else {
            AlarmScheduler.scheduleSnooze(this, settings.getSnoozeMinutes());
        }
        saveMode("");
        stopSelf();
    }

    private void saveMode(String mode) {
        getSharedPreferences("service_state", MODE_PRIVATE).edit().putString("mode", mode).commit();
    }

    private void acquireWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) return;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm == null) return;
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StratosSmartWake:monitor");
        // Safety timeout in case firmware kills the alarm flow unexpectedly.
        wakeLock.acquire(60 * 60_000L);
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        wakeLock = null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        motion.stop();
        heartRate.stop();
        vibration.stop();
        if (logger != null) logger.close();
        releaseWakeLock();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
