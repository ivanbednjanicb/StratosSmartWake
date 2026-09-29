package hr.stratosx.smartwake.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import hr.stratosx.smartwake.SmartWakeSettings;

public final class AlarmScheduler {
    public static final String EXTRA_KIND = "kind";
    public static final String KIND_WINDOW = "window";
    public static final String KIND_HARD = "hard";
    public static final String KIND_SNOOZE = "snooze";
    public static final String KIND_TEST = "test";

    private static final int REQ_WINDOW = 2001;
    private static final int REQ_HARD = 2002;
    private static final int REQ_SNOOZE = 2003;
    private static final int REQ_TEST = 2004;

    private AlarmScheduler() { }

    public static void scheduleNext(Context context) {
        SmartWakeSettings settings = new SmartWakeSettings(context);
        long snoozeAt = context.getSharedPreferences("alarm_state", Context.MODE_PRIVATE).getLong("snooze_at", 0);
        cancelAll(context);
        if (!settings.isEnabled()) return;
        if (snoozeAt > System.currentTimeMillis()) {
            context.getSharedPreferences("alarm_state", Context.MODE_PRIVATE).edit().putLong("snooze_at", snoozeAt).commit();
            schedule(context, REQ_SNOOZE, KIND_SNOOZE, snoozeAt);
            return;
        }

        long now = System.currentTimeMillis();
        long hard = settings.nextHardAlarmMillis(now);
        long start = hard - settings.getWindowMinutes() * 60_000L;
        if (start <= now) start = now + 1500L;

        schedule(context, REQ_WINDOW, KIND_WINDOW, start);
        schedule(context, REQ_HARD, KIND_HARD, hard);
    }


    public static void scheduleFollowingDay(Context context) {
        SmartWakeSettings settings = new SmartWakeSettings(context);
        cancelAll(context);
        if (!settings.isEnabled()) return;

        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(java.util.Calendar.DAY_OF_YEAR, 1);
        c.set(java.util.Calendar.HOUR_OF_DAY, settings.getHour());
        c.set(java.util.Calendar.MINUTE, settings.getMinute());
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        long hard = c.getTimeInMillis();
        long start = hard - settings.getWindowMinutes() * 60_000L;
        schedule(context, REQ_WINDOW, KIND_WINDOW, start);
        schedule(context, REQ_HARD, KIND_HARD, hard);
    }

    public static void scheduleSnooze(Context context, int minutes) {
        cancel(context, REQ_HARD, KIND_HARD);
        cancel(context, REQ_WINDOW, KIND_WINDOW);
        cancel(context, REQ_SNOOZE, KIND_SNOOZE);
        long at = System.currentTimeMillis() + minutes * 60_000L;
        context.getSharedPreferences("alarm_state", Context.MODE_PRIVATE).edit().putLong("snooze_at", at).commit();
        schedule(context, REQ_SNOOZE, KIND_SNOOZE, at);
    }

    /** Schedules a one-off test without touching the real Smart Wake alarms. */
    public static void scheduleTest(Context context, long delayMs) {
        cancel(context, REQ_TEST, KIND_TEST);
        schedule(context, REQ_TEST, KIND_TEST, System.currentTimeMillis() + Math.max(1000L, delayMs));
    }

    public static void cancelTest(Context context) {
        cancel(context, REQ_TEST, KIND_TEST);
    }

    public static void cancelHardAlarm(Context context) {
        cancel(context, REQ_HARD, KIND_HARD);
    }

    public static void cancelAll(Context context) {
        context.getSharedPreferences("alarm_state", Context.MODE_PRIVATE).edit().remove("snooze_at").commit();
        cancel(context, REQ_WINDOW, KIND_WINDOW);
        cancel(context, REQ_HARD, KIND_HARD);
        cancel(context, REQ_SNOOZE, KIND_SNOOZE);
    }

    private static void schedule(Context context, int requestCode, String kind, long atMs) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(context, requestCode, kind);
        am.setExact(AlarmManager.RTC_WAKEUP, atMs, pi);
    }

    private static void cancel(Context context, int requestCode, String kind) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pending(context, requestCode, kind));
    }

    private static PendingIntent pending(Context context, int requestCode, String kind) {
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra(EXTRA_KIND, kind);
        return PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
