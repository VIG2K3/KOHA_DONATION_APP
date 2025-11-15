package com.example.navbotdialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class OTPLoginActivity extends AppCompatActivity {

    private EditText emailInput, otpInput;
    private Button sendOtpButton, verifyOtpButton;

    private String generatedOtp = "";
    private static final String FUNCTION_URL =  "https://sendotpemail-7znmul6tfa-uc.a.run.app";
    //Replace with your own Firebase Cloud Function URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_login);

        // Debug log to check if activity loads
        android.util.Log.d("OTP_DEBUG", "OTPLoginActivity loaded");

        emailInput = findViewById(R.id.emailInput);
        otpInput = findViewById(R.id.otpInput);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        Button backButton = findViewById(R.id.backButton);

        // 🔹 Back button
        backButton.setOnClickListener(v -> {
            android.util.Log.d("OTP_DEBUG", "Back button clicked");
            finish();
        });

        // 🔹 Send OTP button
        sendOtpButton.setOnClickListener(v -> {
            android.util.Log.d("OTP_DEBUG", "Send OTP button clicked");

            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            generatedOtp = String.format("%06d", new Random().nextInt(999999));
            android.util.Log.d("OTP_DEBUG", "Generated OTP: " + generatedOtp);

            new Thread(() -> sendOtpToEmail(email, generatedOtp)).start();
            Toast.makeText(this, "Sending OTP to " + email, Toast.LENGTH_SHORT).show();
        });

        // 🔹 Verify OTP button
        verifyOtpButton.setOnClickListener(v -> {
            android.util.Log.d("OTP_DEBUG", "Verify OTP button clicked");

            String enteredOtp = otpInput.getText().toString().trim();

            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            } else if (enteredOtp.equals(generatedOtp)) {
                Toast.makeText(this, "OTP Verified! Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OTPLoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid OTP, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Sends OTP to Firebase Function
    private void sendOtpToEmail(String email, String otp) {
        try {
            URL url = new URL(FUNCTION_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(10000); // ⏳ 10s timeout
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            String jsonInput = "{\"email\":\"" + email + "\",\"otp\":\"" + otp + "\"}";
            android.util.Log.d("OTP_DEBUG", "Sending to: " + FUNCTION_URL + " | Data: " + jsonInput);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            android.util.Log.d("OTP_DEBUG", "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                android.util.Log.d("OTP_RESPONSE", "Response: " + response.toString());

                runOnUiThread(() ->
                        Toast.makeText(this, "✅ OTP sent successfully to " + email, Toast.LENGTH_SHORT).show());
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                android.util.Log.e("OTP_ERROR", "Server Error (" + responseCode + "): " + errorResponse);

                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Failed to send OTP (code: " + responseCode + ")", Toast.LENGTH_SHORT).show());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("OTP_ERROR", "Exception: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(this, "⚠️ Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
