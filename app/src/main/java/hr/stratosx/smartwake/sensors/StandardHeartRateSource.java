package hr.stratosx.smartwake.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public final class StandardHeartRateSource implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor heartRateSensor;
    private volatile Integer latestBpm;
    private volatile long latestAtMs;

    public StandardHeartRateSource(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) : null;
    }

    public boolean isAvailable() { return heartRateSensor != null; }

    public void start() {
        if (heartRateSensor != null) {
            try {
                sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (SecurityException denied) {
                android.util.Log.w("StratosSmartWake", "HR permission denied; using Huami/motion fallback", denied);
            }
        }
    }

    public void stop() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    public Integer getRecentBpm(long nowMs, long maxAgeMs) {
        if (latestBpm == null || nowMs - latestAtMs > maxAgeMs) return null;
        return latestBpm;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_HEART_RATE || event.values.length == 0) return;
        int bpm = Math.round(event.values[0]);
        if (bpm >= 30 && bpm <= 220) {
            latestBpm = bpm;
            latestAtMs = System.currentTimeMillis();
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
