package com.example.navbotdialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class UserQRCode extends AppCompatActivity {

    ImageView qrCodeImage;
    Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_qrcode);

        qrCodeImage = findViewById(R.id.qrCodeImage);
        closeButton = findViewById(R.id.closeButton);

        // Generate unique QR using Firebase UID
        String userId = FirebaseAuth.getInstance().getUid();
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 600, 600);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close button click with slide-down animation
        closeButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, R.anim.slide_down); // Slide down exit
        });
    }
}
