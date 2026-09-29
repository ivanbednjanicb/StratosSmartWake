package hr.stratosx.smartwake.score;

public final class EpochMetrics {
    public final long timestampMs;
    public final double motionRmsG;
    public final double peaksPerMinute;
    public final Integer heartRateBpm;

    public EpochMetrics(long timestampMs, double motionRmsG, double peaksPerMinute, Integer heartRateBpm) {
        this.timestampMs = timestampMs;
        this.motionRmsG = motionRmsG;
        this.peaksPerMinute = peaksPerMinute;
        this.heartRateBpm = heartRateBpm;
    }
}
