package hr.stratosx.smartwake.sensors;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/** Reads Huami's historical/current HR provider used on Pace/Stratos firmware. */
public final class HuamiHeartRateSource {
    private static final Uri URI = Uri.parse("content://com.huami.watch.health.heartdata");
    private final ContentResolver resolver;

    public static final class Reading {
        public final int bpm;
        public final long timestampMs;
        Reading(int bpm, long timestampMs) {
            this.bpm = bpm;
            this.timestampMs = timestampMs;
        }
    }

    public HuamiHeartRateSource(Context context) {
        resolver = context.getContentResolver();
    }

    public Reading readLatest() {
        Cursor c = null;
        try {
            c = resolver.query(URI, null, null, null, "utc_time DESC");
            if (c == null || !c.moveToFirst()) return null;

            int hrIndex = c.getColumnIndex("heart_rate");
            int timeIndex = c.getColumnIndex("utc_time");
            if (hrIndex < 0 && c.getColumnCount() > 2) hrIndex = 2;
            if (timeIndex < 0 && c.getColumnCount() > 0) timeIndex = 0;
            if (hrIndex < 0 || timeIndex < 0) return null;

            int bpm = c.getInt(hrIndex);
            long ts = c.getLong(timeIndex);
            // Handle both seconds and milliseconds defensively.
            if (ts > 0 && ts < 10_000_000_000L) ts *= 1000L;
            if (bpm < 30 || bpm > 220) return null;
            return new Reading(bpm, ts);
        } catch (RuntimeException unavailable) {
            return null;
        } finally {
            if (c != null) {
                try { c.close(); } catch (RuntimeException ignored) { }
            }
        }
    }
}
