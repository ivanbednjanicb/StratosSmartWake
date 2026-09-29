package hr.stratosx.smartwake.score;

public final class WakeScoreResult {
    public final long timestampMs;
    public final int motionScore;
    public final int heartRateScore;
    public final int transitionScore;
    public final int wakeScore;
    public final double heartRateBaseline;
    public final boolean shouldWake;

    public WakeScoreResult(long timestampMs, int motionScore, int heartRateScore,
                           int transitionScore, int wakeScore, double heartRateBaseline,
                           boolean shouldWake) {
        this.timestampMs = timestampMs;
        this.motionScore = motionScore;
        this.heartRateScore = heartRateScore;
        this.transitionScore = transitionScore;
        this.wakeScore = wakeScore;
        this.heartRateBaseline = heartRateBaseline;
        this.shouldWake = shouldWake;
    }
}
