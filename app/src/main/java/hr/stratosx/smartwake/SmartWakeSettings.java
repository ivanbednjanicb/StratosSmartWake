package hr.stratosx.smartwake;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public final class SmartWakeSettings {
    private static final String PREFS = "smartwake";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_WINDOW = "window_min";
    private static final String KEY_SENSITIVITY = "sensitivity";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_SNOOZE = "snooze_min";

    public enum Sensitivity {
        GENTLE(75), NORMAL(68), AGGRESSIVE(60);
        public final int threshold;
        Sensitivity(int threshold) { this.threshold = threshold; }
    }

    public enum VibrationProfile {
        STRONG, NORMAL, GENTLE
    }

    private final SharedPreferences prefs;

    public SmartWakeSettings(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() { return prefs.getBoolean(KEY_ENABLED, false); }
    public void setEnabled(boolean value) { prefs.edit().putBoolean(KEY_ENABLED, value).apply(); }

    public int getHour() { return prefs.getInt(KEY_HOUR, 7); }
    public int getMinute() { return prefs.getInt(KEY_MINUTE, 0); }
    public void setAlarmTime(int hour, int minute) {
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply();
    }

    public int getWindowMinutes() { return prefs.getInt(KEY_WINDOW, 30); }
    public void setWindowMinutes(int minutes) { prefs.edit().putInt(KEY_WINDOW, minutes).apply(); }

    public Sensitivity getSensitivity() {
        try { return Sensitivity.valueOf(prefs.getString(KEY_SENSITIVITY, Sensitivity.NORMAL.name())); }
        catch (Exception ignored) { return Sensitivity.NORMAL; }
    }
    public void setSensitivity(Sensitivity value) { prefs.edit().putString(KEY_SENSITIVITY, value.name()).apply(); }

    public VibrationProfile getVibrationProfile() {
        try { return VibrationProfile.valueOf(prefs.getString(KEY_VIBRATION, VibrationProfile.NORMAL.name())); }
        catch (Exception ignored) { return VibrationProfile.NORMAL; }
    }
    public void setVibrationProfile(VibrationProfile value) { prefs.edit().putString(KEY_VIBRATION, value.name()).apply(); }

    public int getSnoozeMinutes() { return prefs.getInt(KEY_SNOOZE, 10); }
    public void setSnoozeMinutes(int minutes) { prefs.edit().putInt(KEY_SNOOZE, minutes).apply(); }

    public long nextHardAlarmMillis(long now) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY, getHour());
        c.set(Calendar.MINUTE, getMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= now) c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTimeInMillis();
    }
}
