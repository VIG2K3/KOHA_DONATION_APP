package com.example.navbotdialog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText, loginWithOTPText, forgotPasswordText;
    private Button loginButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.loginRedirectText);
        loginWithOTPText = findViewById(R.id.loginWithOTPText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText); // Make sure this ID exists in XML!

        // ---- Normal Email-Password Login ----
        loginButton.setOnClickListener(view -> {
            String email = loginEmail.getText().toString();
            String pass = loginPassword.getText().toString();

            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this,com.example.navbotdialog.MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    loginPassword.setError("Password cannot be empty");
                }
            } else if (email.isEmpty()) {
                loginEmail.setError("Email cannot be empty");
            } else {
                loginEmail.setError("Please enter valid email");
            }
        });

        // ---- Redirect to Sign Up ----
        signupRedirectText.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        // ---- Login with OTP ----
        loginWithOTPText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, OTPLoginActivity.class);
            startActivity(intent);
        });

        // ---- Forgot Password ----
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Reset Password");

                // Create an EditText inside the dialog
                final EditText resetMail = new EditText(LoginActivity.this);
                resetMail.setHint("Enter your registered email");
                resetMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                resetMail.setPadding(50, 30, 50, 30);

                builder.setView(resetMail);

                // Set dialog buttons
                builder.setPositiveButton("Send Reset Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = resetMail.getText().toString().trim();

                        if (email.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Firebase send reset email
                        auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(LoginActivity.this,
                                                "Password reset link sent to your email",
                                                Toast.LENGTH_LONG).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(LoginActivity.this,
                                                "Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                builder.create().show();
            }
        });

    }
}
