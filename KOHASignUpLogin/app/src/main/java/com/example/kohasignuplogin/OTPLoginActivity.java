package com.example.kohasignuplogin;

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
    private static final String FUNCTION_URL = "https://us-central1-koha-signup-login.cloudfunctions.net/sendOtpEmail";
    // 🔹 Replace with your own Firebase Cloud Function URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_login);

        emailInput = findViewById(R.id.emailInput);
        otpInput = findViewById(R.id.otpInput);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        Button backButton = findViewById(R.id.backButton); // 🟢 Add this line

        // 🔹 Back button functionality
        backButton.setOnClickListener(v -> {
            finish(); // Close this activity and return to the previous screen
        });

        // 🔹 Send OTP button
        sendOtpButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a random 6-digit OTP
            generatedOtp = String.format("%06d", new Random().nextInt(999999));

            // Send it via Firebase Function or SMTP
            new Thread(() -> sendOtpToEmail(email, generatedOtp)).start();
        });

        // 🔹 Verify OTP button
        verifyOtpButton.setOnClickListener(v -> {
            String enteredOtp = otpInput.getText().toString().trim();

            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            } else if (enteredOtp.equals(generatedOtp)) {
                Toast.makeText(this, "OTP Verified! Login Successful", Toast.LENGTH_SHORT).show();
                // Redirect to MainActivity
                Intent intent = new Intent(OTPLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the OTP login screen
            } else {
                Toast.makeText(this, "Invalid OTP, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Sends OTP to Firebase Function
    private void sendOtpToEmail(String email, String otp) {
        try {
            URL url = new URL(FUNCTION_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String jsonInput = "{\"email\":\"" + email + "\",\"otp\":\"" + otp + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                runOnUiThread(() ->
                        Toast.makeText(this, "OTP sent successfully to " + email, Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to send OTP (code: " + responseCode + ")", Toast.LENGTH_SHORT).show());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
