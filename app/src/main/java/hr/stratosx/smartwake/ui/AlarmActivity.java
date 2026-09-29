package hr.stratosx.smartwake.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hr.stratosx.smartwake.SmartWakeSettings;
import hr.stratosx.smartwake.alarm.SmartWakeService;

public final class AlarmActivity extends Activity {
    private boolean testAlarm;
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        recreate();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        boolean smart = getIntent().getBooleanExtra(SmartWakeService.EXTRA_SMART, false);
        testAlarm = getIntent().getBooleanExtra(SmartWakeService.EXTRA_TEST, false);
        SmartWakeSettings settings = new SmartWakeSettings(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(18, 10, 18, 10);
        root.setBackgroundColor(Color.BLACK);

        TextView title = tv(testAlarm ? "TEST ALARMA" : (smart ? "SMART WAKE" : "ALARM"), 20, Color.LTGRAY);
        root.addView(title);

        TextView time = tv(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()), 48, Color.WHITE);
        root.addView(time);

        TextView message = tv(testAlarm ? "Ovo ne mijenja pravi alarm" : (smart ? "Povoljan trenutak za buđenje" : "Krajnje vrijeme alarma"), 13, Color.LTGRAY);
        message.setGravity(Gravity.CENTER);
        root.addView(message);

        Button stop = new Button(this);
        stop.setText("STOP");
        stop.setTextSize(18);
        stop.setOnClickListener(v -> sendAndFinish(SmartWakeService.ACTION_STOP));
        root.addView(stop, fullWidth());

        Button snooze = new Button(this);
        snooze.setText("SNOOZE +" + settings.getSnoozeMinutes() + " min");
        snooze.setOnClickListener(v -> sendAndFinish(SmartWakeService.ACTION_SNOOZE));
        root.addView(snooze, fullWidth());

        setContentView(root);
    }

    private void sendAndFinish(String action) {
        Intent i = new Intent(this, SmartWakeService.class);
        i.setAction(action);
        i.putExtra(SmartWakeService.EXTRA_TEST, testAlarm);
        startService(i);
        finish();
    }

    private TextView tv(String s, float sp, int color) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override public void onBackPressed() { /* Alarm must be explicitly stopped or snoozed. */ }
}
