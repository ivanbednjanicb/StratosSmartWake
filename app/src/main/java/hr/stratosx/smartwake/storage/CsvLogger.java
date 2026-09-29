package hr.stratosx.smartwake.storage;

import android.os.Environment;

import hr.stratosx.smartwake.score.EpochMetrics;
import hr.stratosx.smartwake.score.WakeScoreResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CsvLogger {
    private final File file;
    private FileWriter writer;

    public CsvLogger(long startedMs) {
        File root = new File(Environment.getExternalStorageDirectory(), "StratosSmartWake/logs");
        try {
            if (!root.exists() && !root.mkdirs()) android.util.Log.w("StratosSmartWake", "CSV directory unavailable");
        } catch (SecurityException denied) {
            android.util.Log.w("StratosSmartWake", "CSV storage denied", denied);
        }
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(startedMs)) + ".csv";
        file = new File(root, name);
    }

    public synchronized void open() {
        try {
            writer = new FileWriter(file, true);
            if (file.length() == 0) {
                writer.write("timestamp_ms,motion_rms_g,peaks_per_min,heart_rate_bpm,motion_score,hr_score,transition_score,wake_score,hr_baseline,should_wake\n");
                writer.flush();
            }
        } catch (IOException | SecurityException unavailable) {
            android.util.Log.w("StratosSmartWake", "Cannot open CSV log", unavailable);
        }
    }

    public synchronized void log(EpochMetrics m, WakeScoreResult r) {
        if (writer == null) return;
        try {
            writer.write(m.timestampMs + "," +
                    String.format(Locale.US, "%.5f", m.motionRmsG) + "," +
                    String.format(Locale.US, "%.2f", m.peaksPerMinute) + "," +
                    (m.heartRateBpm == null ? "" : m.heartRateBpm) + "," +
                    r.motionScore + "," + r.heartRateScore + "," + r.transitionScore + "," +
                    r.wakeScore + "," +
                    (Double.isNaN(r.heartRateBaseline) ? "" : String.format(Locale.US, "%.2f", r.heartRateBaseline)) + "," +
                    r.shouldWake + "\n");
            writer.flush();
        } catch (IOException ignored) { }
    }

    public synchronized void event(String event) {
        if (writer == null) return;
        try {
            writer.write("# " + System.currentTimeMillis() + " " + event.replace('\n', ' ') + "\n");
            writer.flush();
        } catch (IOException ignored) { }
    }

    public synchronized void close() {
        if (writer == null) return;
        try { writer.close(); } catch (IOException ignored) { }
        writer = null;
    }

    public File getFile() { return file; }
}
