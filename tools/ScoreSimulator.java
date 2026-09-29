import hr.stratosx.smartwake.score.EpochMetrics;
import hr.stratosx.smartwake.score.WakeScoreEngine;
import hr.stratosx.smartwake.score.WakeScoreResult;

public class ScoreSimulator {
    public static void main(String[] args) {
        long start = 1_000_000L;
        WakeScoreEngine engine = new WakeScoreEngine(68, start);

        double[] motion = {0.012,0.014,0.015,0.018,0.022,0.028,0.040,0.060,0.082,0.095};
        double[] peaks  = {0.5,0.5,1,1,2,3,5,8,10,12};
        int[] hr         = {55,55,54,55,56,57,59,62,64,66};

        for (int i=0; i<motion.length; i++) {
            long ts = start + (i+1)*30_000L;
            WakeScoreResult r = engine.update(new EpochMetrics(ts, motion[i], peaks[i], hr[i]));
            System.out.printf("%2d  M=%3d HR=%3d T=%3d W=%3d wake=%s baseline=%.1f%n",
                    i+1, r.motionScore, r.heartRateScore, r.transitionScore,
                    r.wakeScore, r.shouldWake, r.heartRateBaseline);
        }
    }
}
