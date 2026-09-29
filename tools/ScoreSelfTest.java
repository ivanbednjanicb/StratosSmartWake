import hr.stratosx.smartwake.score.EpochMetrics;
import hr.stratosx.smartwake.score.WakeScoreEngine;
import hr.stratosx.smartwake.score.WakeScoreResult;

public final class ScoreSelfTest {
    private static int passed;

    public static void main(String[] args) {
        testSingleSpikeDoesNotWake();
        testSustainedTransitionWakes();
        testMotionOnlyFallback();
        testNoImmediateWake();
        testTwoOfThreeAndBoundary();
        System.out.println("PASS: " + passed + "/5 Smart Wake engine tests");
    }

    private static void testSingleSpikeDoesNotWake() {
        long t = 1_000_000L;
        WakeScoreEngine e = new WakeScoreEngine(68, t);
        feed(e, t, 0.01, 0.5, 55);
        feed(e, t + 30_000, 0.015, 1, 55);
        feed(e, t + 60_000, 0.02, 2, 56);
        WakeScoreResult spike = feed(e, t + 90_000, 0.12, 15, 72);
        WakeScoreResult calm = feed(e, t + 120_000, 0.015, 1, 56);
        require(!spike.shouldWake && !calm.shouldWake, "single spike must not wake");
    }

    private static void testSustainedTransitionWakes() {
        long t = 2_000_000L;
        WakeScoreEngine e = new WakeScoreEngine(68, t);
        for (int i = 0; i < 5; i++) feed(e, t + i * 30_000L, 0.01 + i * .002, 1 + i * .4, 55 + (i > 3 ? 1 : 0));
        feed(e, t + 150_000, 0.04, 4, 58);
        WakeScoreResult firstHigh = feed(e, t + 180_000, 0.07, 8, 62);
        WakeScoreResult confirmed = feed(e, t + 210_000, 0.10, 12, 66);
        require(!firstHigh.shouldWake && confirmed.shouldWake, "sustained transition should wake after confirmation");
    }

    private static void testMotionOnlyFallback() {
        long t = 3_000_000L;
        WakeScoreEngine e = new WakeScoreEngine(60, t);
        feed(e, t, 0.01, 0.5, null);
        feed(e, t + 30_000, 0.01, 0.5, null);
        feed(e, t + 60_000, 0.02, 1, null);
        feed(e, t + 90_000, 0.10, 13, null);
        WakeScoreResult r = feed(e, t + 120_000, 0.12, 15, null);
        require(r.shouldWake, "motion-only mode should remain functional");
    }

    private static void testNoImmediateWake() {
        long t = 4_000_000L;
        WakeScoreEngine e = new WakeScoreEngine(20, t);
        WakeScoreResult a = feed(e, t, 0.12, 15, 90);
        WakeScoreResult b = feed(e, t + 30_000, 0.12, 15, 90);
        require(!a.shouldWake && !b.shouldWake, "90-second minimum must block immediate wake");
    }

    private static WakeScoreResult feed(WakeScoreEngine e, long t, double rms, double peaks, Integer hr) {
        return e.update(new EpochMetrics(t, rms, peaks, hr));
    }

    private static void testTwoOfThreeAndBoundary() {
        long t = 5_000_000L;
        WakeScoreEngine e = new WakeScoreEngine(60, t);
        feed(e, t + 30_000, .12, 15, null);
        boolean quiet = !feed(e, t + 60_000, .01, .5, null).shouldWake;
        boolean blocked = !feed(e, t + 89_999, .12, 15, null).shouldWake;
        boolean boundary = feed(e, t + 90_000, .12, 15, null).shouldWake;
        WakeScoreEngine separated = new WakeScoreEngine(60, t);
        feed(separated, t + 90_000, .12, 15, null);
        feed(separated, t + 120_000, .01, .5, null);
        require(quiet && blocked && boundary && feed(separated, t + 150_000, .12, 15, null).shouldWake,
                "90 second boundary and nonconsecutive two of three confirmation");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        passed++;
    }
}
