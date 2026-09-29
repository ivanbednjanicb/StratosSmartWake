package hr.stratosx.smartwake.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public final class MotionCollector implements SensorEventListener {
    private static final double G = SensorManager.GRAVITY_EARTH;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    private long sampleCount;
    private double sumSquareG;
    private long peakCount;
    private long epochStartedMs;

    public static final class Snapshot {
        public final double rmsG;
        public final double peaksPerMinute;
        public final long samples;

        Snapshot(double rmsG, double peaksPerMinute, long samples) {
            this.rmsG = rmsG;
            this.peaksPerMinute = peaksPerMinute;
            this.samples = samples;
        }
    }

    public MotionCollector(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
    }

    public boolean isAvailable() { return accelerometer != null; }

    public void start() {
        reset(System.currentTimeMillis());
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stop() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    public synchronized Snapshot snapshotAndReset(long nowMs) {
        long elapsedMs = Math.max(1L, nowMs - epochStartedMs);
        double rms = sampleCount == 0 ? 0.0 : Math.sqrt(sumSquareG / sampleCount);
        double peaksPerMinute = peakCount * (60_000.0 / elapsedMs);
        Snapshot result = new Snapshot(rms, peaksPerMinute, sampleCount);
        reset(nowMs);
        return result;
    }

    private synchronized void reset(long nowMs) {
        sampleCount = 0;
        sumSquareG = 0;
        peakCount = 0;
        epochStartedMs = nowMs;
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || event.values.length < 3) return;
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        double dynamicG = Math.abs(magnitude - G) / G;
        sumSquareG += dynamicG * dynamicG;
        sampleCount++;
        if (dynamicG >= 0.10) peakCount++;
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
