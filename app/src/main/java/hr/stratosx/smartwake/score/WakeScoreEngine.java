package hr.stratosx.smartwake.score;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Deliberately simple and explainable v0.25 classifier.
 * It does NOT claim to identify medical sleep stages. It estimates wake-readiness
 * from movement, relative HR rise and a transition out of a stable period.
 */
public final class WakeScoreEngine {
    private final int threshold;
    private final long monitoringStartedMs;
    private final Deque<Integer> recentWakeScores = new ArrayDeque<>();
    private final Deque<Integer> recentMotionScores = new ArrayDeque<>();
    private final List<Integer> initialHeartRates = new ArrayList<>();

    private double heartRateBaseline = Double.NaN;
    private int epochCount = 0;

    public WakeScoreEngine(int threshold, long monitoringStartedMs) {
        this.threshold = threshold;
        this.monitoringStartedMs = monitoringStartedMs;
    }

    public WakeScoreResult update(EpochMetrics metrics) {
        epochCount++;
        int motionScore = calculateMotion(metrics.motionRmsG, metrics.peaksPerMinute);
        int hrScore = calculateHeartRate(metrics.heartRateBpm);
        int transitionScore = calculateTransition(motionScore, hrScore);

        double weighted;
        if (metrics.heartRateBpm == null || Double.isNaN(heartRateBaseline)) {
            // Motion-only fallback: don't punish the watch if HR is unavailable.
            weighted = 0.72 * motionScore + 0.28 * transitionScore;
        } else {
            weighted = 0.50 * motionScore + 0.30 * hrScore + 0.20 * transitionScore;
        }
        int wakeScore = clamp((int)Math.round(weighted), 0, 100);

        recentWakeScores.addLast(wakeScore);
        while (recentWakeScores.size() > 3) recentWakeScores.removeFirst();

        // Avoid a false wake from a single movement immediately after monitoring starts.
        boolean enoughTime = metrics.timestampMs - monitoringStartedMs >= 90_000L;
        int above = 0;
        for (Integer s : recentWakeScores) if (s >= threshold) above++;
        boolean shouldWake = enoughTime && recentWakeScores.size() >= 2 && above >= 2;

        recentMotionScores.addLast(motionScore);
        while (recentMotionScores.size() > 6) recentMotionScores.removeFirst();

        return new WakeScoreResult(metrics.timestampMs, motionScore, hrScore,
                transitionScore, wakeScore, heartRateBaseline, shouldWake);
    }

    private int calculateMotion(double rmsG, double peaksPerMinute) {
        // Conservative initial calibration; intended to be tuned from real Stratos logs.
        double rms = scale(rmsG, 0.010, 0.120);
        double peaks = scale(peaksPerMinute, 0.5, 14.0);
        return clamp((int)Math.round(100.0 * (0.72 * rms + 0.28 * peaks)), 0, 100);
    }

    private int calculateHeartRate(Integer bpm) {
        if (bpm == null || bpm < 30 || bpm > 220) return 0;

        if (initialHeartRates.size() < 8) {
            initialHeartRates.add(bpm);
            heartRateBaseline = median(initialHeartRates);
        } else if (!Double.isNaN(heartRateBaseline) && bpm <= heartRateBaseline + 5) {
            // Slowly follow downward/stable night HR, but don't let a wake-up rise erase the baseline.
            heartRateBaseline = 0.97 * heartRateBaseline + 0.03 * bpm;
        }

        if (Double.isNaN(heartRateBaseline) || heartRateBaseline <= 0) return 0;
        double relativeRise = (bpm - heartRateBaseline) / heartRateBaseline;
        return clamp((int)Math.round(100.0 * scale(relativeRise, 0.02, 0.16)), 0, 100);
    }

    private int calculateTransition(int currentMotion, int hrScore) {
        if (recentMotionScores.size() < 3) {
            return clamp((int)Math.round(0.6 * currentMotion + 0.4 * hrScore), 0, 100);
        }

        Integer[] arr = recentMotionScores.toArray(new Integer[0]);
        int split = Math.max(1, arr.length - 2);
        double older = average(arr, 0, split);
        double recent = average(arr, split, arr.length);
        double delta = currentMotion - older;
        double trend = 100.0 * scale(delta, 8.0, 42.0);

        return clamp((int)Math.round(0.72 * trend + 0.28 * hrScore), 0, 100);
    }

    private static double average(Integer[] values, int from, int to) {
        if (to <= from) return 0;
        double sum = 0;
        for (int i = from; i < to; i++) sum += values[i];
        return sum / (to - from);
    }

    private static double median(List<Integer> values) {
        List<Integer> copy = new ArrayList<>(values);
        java.util.Collections.sort(copy);
        int n = copy.size();
        if (n == 0) return Double.NaN;
        if ((n & 1) == 1) return copy.get(n / 2);
        return (copy.get(n / 2 - 1) + copy.get(n / 2)) / 2.0;
    }

    private static double scale(double value, double low, double high) {
        if (value <= low) return 0.0;
        if (value >= high) return 1.0;
        return (value - low) / (high - low);
    }

    private static int clamp(int value, int low, int high) {
        return Math.max(low, Math.min(high, value));
    }
}
