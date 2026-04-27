package com.example.to_dolistapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton add;
    AlertDialog dialog;
    LinearLayout layout;
    TextView taskCount, weatherTemp, weatherDesc, weatherIcon, weatherHigh, weatherLow;

    // Priority selection state in the dialog
    String selectedPriority = "Medium";

    FusedLocationProviderClient fusedLocationClient;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        add = findViewById(R.id.add);
        layout = findViewById(R.id.container);
        taskCount = findViewById(R.id.taskCount);
        weatherTemp = findViewById(R.id.weatherTemp);
        weatherDesc = findViewById(R.id.weatherDesc);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherHigh = findViewById(R.id.weatherHigh);
        weatherLow = findViewById(R.id.weatherLow);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        buildDialog();

        add.setOnClickListener(v -> dialog.show());

        fetchWeather();
    }

    // ─── Weather ────────────────────────────────────────────────────────────────

    private void fetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            weatherDesc.setText("Enable location for weather");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                callWeatherApi(location.getLatitude(), location.getLongitude());
            } else {
                weatherDesc.setText("Location unavailable");
            }
        });
    }

    private void callWeatherApi(double lat, double lon) {
        // Open-Meteo: completely free, no API key required
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,weathercode,windspeed_10m"
                + "&daily=temperature_2m_max,temperature_2m_min"
                + "&temperature_unit=fahrenheit"
                + "&forecast_days=1"
                + "&timezone=auto";

        executor.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONObject current = json.getJSONObject("current");
                JSONObject daily = json.getJSONObject("daily");

                double temp = current.getDouble("temperature_2m");
                int code = current.getInt("weathercode");
                double high = daily.getJSONArray("temperature_2m_max").getDouble(0);
                double low = daily.getJSONArray("temperature_2m_min").getDouble(0);

                String icon = getWeatherIcon(code);
                String desc = getWeatherDescription(code);

                mainHandler.post(() -> {
                    weatherTemp.setText(Math.round(temp) + "°F  " + desc);
                    weatherDesc.setText("Wind: " + Math.round(current.optDouble("windspeed_10m")) + " mph");
                    weatherIcon.setText(icon);
                    weatherHigh.setText("H: " + Math.round(high) + "°");
                    weatherLow.setText("L: " + Math.round(low) + "°");
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    weatherTemp.setText("Weather unavailable");
                    weatherDesc.setText("Check internet connection");
                });
            }
        });
    }

    private String getWeatherIcon(int code) {
        if (code == 0) return "☀️";
        if (code <= 2) return "🌤";
        if (code == 3) return "☁️";
        if (code <= 49) return "🌫";
        if (code <= 59) return "🌦";
        if (code <= 69) return "🌧";
        if (code <= 79) return "❄️";
        if (code <= 84) return "🌧";
        if (code <= 94) return "⛈";
        return "🌩";
    }

    private String getWeatherDescription(int code) {
        if (code == 0) return "Clear Sky";
        if (code == 1) return "Mostly Clear";
        if (code == 2) return "Partly Cloudy";
        if (code == 3) return "Overcast";
        if (code <= 49) return "Foggy";
        if (code <= 59) return "Drizzle";
        if (code <= 69) return "Rain";
        if (code <= 79) return "Snow";
        if (code <= 84) return "Rain Showers";
        if (code <= 94) return "Thunderstorm";
        return "Severe Storm";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeather();
        }
    }

    // ─── Dialog ─────────────────────────────────────────────────────────────────

    public void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        final TextInputEditText nameEdit = view.findViewById(R.id.nameEdit);
        View btnLow = view.findViewById(R.id.btnLow);
        View btnMedium = view.findViewById(R.id.btnMedium);
        View btnHigh = view.findViewById(R.id.btnHigh);

        // Default selection
        selectedPriority = "Medium";
        highlightPriorityButton(view, "Medium");

        btnLow.setOnClickListener(v -> { selectedPriority = "Low"; highlightPriorityButton(view, "Low"); });
        btnMedium.setOnClickListener(v -> { selectedPriority = "Medium"; highlightPriorityButton(view, "Medium"); });
        btnHigh.setOnClickListener(v -> { selectedPriority = "High"; highlightPriorityButton(view, "High"); });

        builder.setView(view)
                .setPositiveButton("SAVE", (d, which) -> {
                    String taskName = nameEdit.getText() != null ? nameEdit.getText().toString().trim() : "";
                    if (!taskName.isEmpty()) {
                        addCard(taskName, selectedPriority);
                    }
                    nameEdit.setText("");
                    selectedPriority = "Medium";
                })
                .setNegativeButton("Cancel", null);

        dialog = builder.create();
    }

    private void highlightPriorityButton(View parent, String priority) {
        android.widget.Button btnLow = parent.findViewById(R.id.btnLow);
        android.widget.Button btnMedium = parent.findViewById(R.id.btnMedium);
        android.widget.Button btnHigh = parent.findViewById(R.id.btnHigh);

        // Reset all to dim
        btnLow.setAlpha(0.4f);
        btnMedium.setAlpha(0.4f);
        btnHigh.setAlpha(0.4f);

        // Highlight selected
        switch (priority) {
            case "Low":    btnLow.setAlpha(1.0f); break;
            case "Medium": btnMedium.setAlpha(1.0f); break;
            case "High":   btnHigh.setAlpha(1.0f); break;
        }
    }

    // ─── Cards ───────────────────────────────────────────────────────────────────

    private void addCard(String name, String priority) {
        final View view = getLayoutInflater().inflate(R.layout.card, null);
        TextView nameView = view.findViewById(R.id.name);
        TextView priorityLabel = view.findViewById(R.id.priorityLabel);
        View priorityDot = view.findViewById(R.id.priorityDot);
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        ImageButton delete = view.findViewById(R.id.delete);

        nameView.setText(name);
        priorityLabel.setText(priority + " Priority");

        // Color the dot by priority
        int dotColor;
        switch (priority) {
            case "High":   dotColor = android.graphics.Color.parseColor("#E74C3C"); break;
            case "Low":    dotColor = android.graphics.Color.parseColor("#2ECC71"); break;
            default:       dotColor = android.graphics.Color.parseColor("#F39C12"); break;
        }
        priorityDot.getBackground().setTint(dotColor);
        priorityLabel.setTextColor(dotColor);

        // Strikethrough on complete
        checkbox.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                nameView.setPaintFlags(nameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                nameView.setAlpha(0.4f);
                priorityLabel.setAlpha(0.4f);
            } else {
                nameView.setPaintFlags(nameView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                nameView.setAlpha(1.0f);
                priorityLabel.setAlpha(1.0f);
            }
        });

        delete.setOnClickListener(v -> {
            layout.removeView(view);
            updateTaskCount();
        });

        layout.addView(view);
        updateTaskCount();
    }

    private void updateTaskCount() {
        int count = layout.getChildCount();
        taskCount.setText(count + (count == 1 ? " task today" : " tasks today"));
    }
}
