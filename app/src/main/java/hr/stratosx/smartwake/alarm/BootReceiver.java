package hr.stratosx.smartwake.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.getSharedPreferences("service_state", Context.MODE_PRIVATE).edit().clear().commit();
            AlarmScheduler.scheduleNext(context);
        }
    }
}
