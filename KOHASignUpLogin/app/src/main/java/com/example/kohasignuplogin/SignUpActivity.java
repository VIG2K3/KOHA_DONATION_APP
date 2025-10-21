package com.example.kohasignuplogin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.nio.charset.StandardCharsets;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, signupOtp;
    private Button signupButton, getOtpButton;
    private TextView loginRedirectText;

    private String generatedOtp;
    private long otpGeneratedTime; // store when OTP was generated
    private static final long OTP_VALID_DURATION = 2 * 60 * 1000; // 2 minutes

    // 🔹 Replace with your deployed Cloud Function URL
    private static final String CLOUD_FUNCTION_URL =
            "https://us-central1-koha-signup-login.cloudfunctions.net/sendOtpEmail";

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupOtp = findViewById(R.id.signup_otp);
        signupButton = findViewById(R.id.signup_button);
        getOtpButton = findViewById(R.id.get_otp_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // 🔸 GET OTP button
        getOtpButton.setOnClickListener(view -> {
            String email = signupEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }

            generatedOtp = String.format("%06d", new Random().nextInt(999999));
            otpGeneratedTime = System.currentTimeMillis(); // record generation time
            sendOtpToCloud(email, generatedOtp);
        });

        // 🔹 SIGN UP button
        signupButton.setOnClickListener(view -> {
            String user = signupEmail.getText().toString().trim();
            String pass = signupPassword.getText().toString().trim();
            String otpInput = signupOtp.getText().toString().trim();

            if (user.isEmpty()) {
                signupEmail.setError("Email cannot be empty");
                return;
            }
            if (pass.isEmpty()) {
                signupPassword.setError("Password cannot be empty");
                return;
            }
            if (otpInput.isEmpty()) {
                signupOtp.setError("Enter the OTP");
                return;
            }
            if (!otpInput.equals(generatedOtp)) {
                signupOtp.setError("Invalid OTP");
                return;
            }
            if (System.currentTimeMillis() - otpGeneratedTime > OTP_VALID_DURATION) {
                signupOtp.setError("OTP expired. Please request a new one.");
                return;
            }

            // ✅ Create Firebase user
            auth.createUserWithEmailAndPassword(user, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Sign Up Failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 🔸 Redirect to Login
        loginRedirectText.setOnClickListener(view ->
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class))
        );
    }

    // 📧 Send OTP to Firebase Cloud Function
    private void sendOtpToCloud(String email, String otp) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(CLOUD_FUNCTION_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("otp", otp);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                // ✅ Use manual InputStream reading for compatibility
                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                if (is != null) {
                    int ch;
                    while ((ch = is.read()) != -1) {
                        responseBuilder.append((char) ch);
                    }
                    is.close();
                }

                String response = responseBuilder.toString();

                runOnUiThread(() -> {
                    getOtpButton.setEnabled(true);
                    if (responseCode == 200) {
                        Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send OTP: " + response, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    getOtpButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
