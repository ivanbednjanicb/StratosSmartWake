package hr.stratosx.smartwake.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
        android.os.PowerManager.WakeLock handoff = pm == null ? null
                : pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "StratosSmartWake:handoff");
        if (handoff != null) handoff.acquire(10_000L);
        String kind = intent.getStringExtra(AlarmScheduler.EXTRA_KIND);
        Intent service = new Intent(context, SmartWakeService.class);
        if (AlarmScheduler.KIND_WINDOW.equals(kind)) {
            service.setAction(SmartWakeService.ACTION_START_MONITORING);
        } else if (AlarmScheduler.KIND_TEST.equals(kind)) {
            service.setAction(SmartWakeService.ACTION_TEST);
        } else {
            service.setAction(SmartWakeService.ACTION_FORCE_WAKE);
        }
        context.startService(service);
    }
}
