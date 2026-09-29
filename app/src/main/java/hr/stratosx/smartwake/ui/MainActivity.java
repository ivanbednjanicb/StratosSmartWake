package hr.stratosx.smartwake.ui;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Locale;

import hr.stratosx.smartwake.SmartWakeSettings;
import hr.stratosx.smartwake.alarm.AlarmScheduler;
import hr.stratosx.smartwake.alarm.SmartWakeService;

public final class MainActivity extends Activity {
    private SmartWakeSettings settings;
    private TextView alarmTime;
    private TextView status;
    private Spinner windowSpinner;
    private Spinner sensitivitySpinner;
    private Spinner vibrationSpinner;
    private ToggleButton enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new SmartWakeSettings(this);
        buildUi();
        refresh();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 10, 18, 18);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.BLACK);
        scroll.addView(root);

        TextView title = text("SMART WAKE", 20, Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        alarmTime = text("07:00", 40, Color.WHITE);
        alarmTime.setGravity(Gravity.CENTER);
        alarmTime.setPadding(0, 2, 0, 2);
        alarmTime.setOnClickListener(v -> chooseTime());
        root.addView(alarmTime, matchWrap());

        status = text("", 12, Color.LTGRAY);
        status.setGravity(Gravity.CENTER);
        root.addView(status, matchWrap());

        root.addView(label("Prozor buđenja"));
        windowSpinner = spinner(new String[]{"10 min", "20 min", "30 min", "45 min"});
        root.addView(windowSpinner, matchWrap());

        root.addView(label("Osjetljivost"));
        sensitivitySpinner = spinner(new String[]{"Gentle", "Normal", "Aggressive"});
        root.addView(sensitivitySpinner, matchWrap());

        root.addView(label("Vibracija"));
        vibrationSpinner = spinner(new String[]{"Strong ~10 s", "Normal ~22 s", "Gentle ~30 s"});
        root.addView(vibrationSpinner, matchWrap());

        enabled = new ToggleButton(this);
        enabled.setTextOn("UKLJUČEN");
        enabled.setTextOff("ISKLJUČEN");
        root.addView(enabled, matchWrap());

        Button save = button("SPREMI I AKTIVIRAJ");
        save.setOnClickListener(v -> saveAndSchedule());
        root.addView(save, matchWrap());

        Button test = button("TEST VIBRACIJE ODMAH");
        test.setOnClickListener(v -> {
            Intent i = new Intent(this, SmartWakeService.class);
            i.setAction(SmartWakeService.ACTION_TEST);
            startService(i);
        });
        root.addView(test, matchWrap());

        Button scheduledTest = button("TEST ALARMA ZA 2 MIN");
        scheduledTest.setOnClickListener(v -> {
            AlarmScheduler.scheduleTest(this, 2 * 60_000L);
            status.setText("Test zakazan za 2 min · pravi alarm ostaje netaknut");
        });
        root.addView(scheduledTest, matchWrap());

        Button diag = button("SENZORI");
        diag.setOnClickListener(v -> startActivity(new Intent(this, DiagnosticsActivity.class)));
        root.addView(diag, matchWrap());

        setContentView(scroll);
    }

    private void chooseTime() {
        TimePickerDialog dialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        settings.setAlarmTime(hourOfDay, minute);
                        alarmTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
                    }
                }, settings.getHour(), settings.getMinute(), true);
        dialog.show();
    }

    private void saveAndSchedule() {
        stopService(new Intent(this, SmartWakeService.class));
        getSharedPreferences("service_state", MODE_PRIVATE).edit().clear().commit();
        String window = String.valueOf(windowSpinner.getSelectedItem()).replace(" min", "");
        settings.setWindowMinutes(Integer.parseInt(window));
        settings.setSensitivity(SmartWakeSettings.Sensitivity.values()[sensitivitySpinner.getSelectedItemPosition()]);
        settings.setVibrationProfile(SmartWakeSettings.VibrationProfile.values()[vibrationSpinner.getSelectedItemPosition()]);
        settings.setEnabled(enabled.isChecked());
        AlarmScheduler.cancelAll(this);
        AlarmScheduler.scheduleNext(this);
        refresh();
    }

    private void refresh() {
        alarmTime.setText(String.format(Locale.US, "%02d:%02d", settings.getHour(), settings.getMinute()));
        enabled.setChecked(settings.isEnabled());
        windowSpinner.setSelection(indexOfWindow(settings.getWindowMinutes()));
        sensitivitySpinner.setSelection(settings.getSensitivity().ordinal());
        vibrationSpinner.setSelection(settings.getVibrationProfile().ordinal());
        if (settings.isEnabled()) {
            status.setText("Smart Wake aktivan · fallback alarm uvijek radi");
        } else {
            status.setText("Alarm nije aktivan");
        }
    }

    private int indexOfWindow(int value) {
        if (value == 10) return 0;
        if (value == 20) return 1;
        if (value == 45) return 3;
        return 2;
    }

    private TextView label(String s) {
        TextView tv = text(s, 12, Color.LTGRAY);
        tv.setPadding(0, 7, 0, 1);
        return tv;
    }

    private TextView text(String s, float sp, int color) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        return tv;
    }

    private Spinner spinner(String[] items) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        return s;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        return b;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
