package com.example.esp8266;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String ESP_IP = "192.168.18.124"; // Ganti dengan IP ESP Anda
    private Button btnOn, btnOff;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);

        // Tampilkan IP yang digunakan
        tvStatus.setText("Terhubung ke: " + ESP_IP);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ControlTask().execute("on");
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ControlTask().execute("off");
            }
        });

        // Cek koneksi saat aplikasi dibuka
        new CheckConnectionTask().execute();
    }

    private class CheckConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return sendHttpRequest("status");
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && (result.contains("1") || result.contains("0"))) {
                updateButtonStates(true);
                Toast.makeText(MainActivity.this, "ESP8266 Terhubung", Toast.LENGTH_SHORT).show();
            } else {
                updateButtonStates(false);
                Toast.makeText(MainActivity.this, "Gagal terhubung ke ESP8266", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ControlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return sendHttpRequest(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this,
                    result != null ? result : "Gagal mengontrol lampu",
                    Toast.LENGTH_SHORT).show();

            // Memeriksa status terbaru setelah kontrol
            new CheckConnectionTask().execute();
        }
    }

    private String sendHttpRequest(String endpoint) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("http://" + ESP_IP + "/" + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            } else {
                return "Error: HTTP " + responseCode;
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateButtonStates(boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnOn.setEnabled(connected);
                btnOff.setEnabled(connected);
            }
        });
    }
}