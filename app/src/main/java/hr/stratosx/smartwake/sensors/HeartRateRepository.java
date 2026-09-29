package hr.stratosx.smartwake.sensors;

import android.content.Context;

public final class HeartRateRepository {
    private final StandardHeartRateSource standard;
    private final HuamiHeartRateSource huami;
    private String lastSource = "none";

    public HeartRateRepository(Context context) {
        standard = new StandardHeartRateSource(context);
        huami = new HuamiHeartRateSource(context);
    }

    public void start() { standard.start(); }
    public void stop() { standard.stop(); }

    public boolean hasStandardSensor() { return standard.isAvailable(); }
    public String getLastSource() { return lastSource; }

    public Integer getBestBpm(long nowMs) {
        Integer standardBpm = standard.getRecentBpm(nowMs, 120_000L);
        if (standardBpm != null) {
            lastSource = "android";
            return standardBpm;
        }

        HuamiHeartRateSource.Reading reading = huami.readLatest();
        if (reading != null && Math.abs(nowMs - reading.timestampMs) <= 15 * 60_000L) {
            lastSource = "huami";
            return reading.bpm;
        }
        lastSource = "none";
        return null;
    }

    public HuamiHeartRateSource.Reading readHuamiDirect() { return huami.readLatest(); }
}
