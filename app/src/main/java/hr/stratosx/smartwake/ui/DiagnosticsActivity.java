package hr.stratosx.smartwake.ui;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hr.stratosx.smartwake.sensors.HeartRateRepository;
import hr.stratosx.smartwake.sensors.HuamiHeartRateSource;

public final class DiagnosticsActivity extends Activity {
    private TextView output;
    private HeartRateRepository hr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hr = new HeartRateRepository(this);
        buildUi();
        refresh();
    }

    @Override protected void onStart() {
        super.onStart();
        hr.start();
    }

    @Override protected void onStop() {
        hr.stop();
        super.onStop();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(12, 8, 12, 12);
        root.setBackgroundColor(Color.BLACK);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("SENZORI / DIJAGNOSTIKA");
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        Button refresh = new Button(this);
        refresh.setText("OSVJEŽI");
        refresh.setOnClickListener(v -> refresh());
        root.addView(refresh);

        output = new TextView(this);
        output.setTextColor(Color.LTGRAY);
        output.setTextSize(11);
        root.addView(output);
        setContentView(scroll);
    }

    private void refresh() {
        StringBuilder sb = new StringBuilder();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sm != null) {
            List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
            sb.append("Android senzori: ").append(sensors.size()).append("\n\n");
            for (Sensor s : sensors) {
                sb.append(typeName(s.getType())).append("\n  ")
                        .append(s.getName()).append(" / ").append(s.getVendor())
                        .append("\n  range=").append(s.getMaximumRange())
                        .append(" res=").append(s.getResolution()).append("\n\n");
            }
        }

        long now = System.currentTimeMillis();
        Integer best = hr.getBestBpm(now);
        sb.append("Standard HR sensor: ").append(hr.hasStandardSensor() ? "DA" : "NE").append("\n");
        sb.append("Najbolji HR: ").append(best == null ? "nema svježeg podatka" : best + " bpm")
                .append(" [").append(hr.getLastSource()).append("]\n");

        HuamiHeartRateSource.Reading direct = hr.readHuamiDirect();
        if (direct != null) {
            sb.append("Huami provider: ").append(direct.bpm).append(" bpm\n")
                    .append("Vrijeme: ").append(new SimpleDateFormat("HH:mm:ss", Locale.US)
                            .format(new Date(direct.timestampMs))).append("\n");
        } else {
            sb.append("Huami provider: nedostupan / prazan\n");
        }
        output.setText(sb.toString());
    }

    private String typeName(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: return "ACCELEROMETER";
            case Sensor.TYPE_GYROSCOPE: return "GYROSCOPE";
            case Sensor.TYPE_MAGNETIC_FIELD: return "MAGNETOMETER";
            case Sensor.TYPE_PRESSURE: return "PRESSURE";
            case Sensor.TYPE_LIGHT: return "LIGHT";
            case Sensor.TYPE_HEART_RATE: return "HEART_RATE";
            default: return "TYPE " + type;
        }
    }
}
