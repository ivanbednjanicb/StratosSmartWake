package hr.stratosx.smartwake.alarm;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;

import hr.stratosx.smartwake.SmartWakeSettings;

public final class VibrationController {
    private final Vibrator vibrator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable strongLoop = new Runnable() {
        @Override public void run() {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(new long[]{0, 700, 450}, 0);
            }
        }
    };

    public VibrationController(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void start(SmartWakeSettings.VibrationProfile profile) {
        stop();
        if (vibrator == null || !vibrator.hasVibrator()) return;
        long[] intro;
        long delayToStrong;
        switch (profile) {
            case STRONG:
                intro = new long[]{0, 160, 900, 220, 700, 300, 550, 420, 400, 600};
                delayToStrong = 10_000L;
                break;
            case GENTLE:
                intro = new long[]{0, 90, 2300, 100, 2200, 120, 1900, 150, 1700, 180, 1500, 220,
                        1300, 280, 1100, 350, 900, 450, 700, 550, 500, 700};
                delayToStrong = 30_000L;
                break;
            case NORMAL:
            default:
                intro = new long[]{0, 100, 1800, 110, 1700, 130, 1500, 160, 1300, 200, 1100, 250,
                        900, 320, 750, 400, 600, 520, 450, 650};
                delayToStrong = 22_000L;
                break;
        }
        vibrator.vibrate(intro, -1);
        handler.postDelayed(strongLoop, delayToStrong);
    }

    public void stop() {
        handler.removeCallbacks(strongLoop);
        if (vibrator != null) vibrator.cancel();
    }
}
