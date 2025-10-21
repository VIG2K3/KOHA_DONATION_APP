package com.example.kohasignuplogin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OpeningActivity extends AppCompatActivity {

    private Button loginButton, signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(OpeningActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(OpeningActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}