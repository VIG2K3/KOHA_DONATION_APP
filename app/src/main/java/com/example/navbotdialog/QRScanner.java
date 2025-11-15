package com.example.navbotdialog;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRScanner extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        // Force portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        barcodeView = findViewById(R.id.barcode_scanner);
        closeButton = findViewById(R.id.closeButton);

        // Start scanning QR codes only
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    Toast.makeText(QRScanner.this, "Scanned: " + result.getText(), Toast.LENGTH_LONG).show();
                    finish(); // close after scan
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {}
        });

        // Close button
        closeButton.setOnClickListener(v -> {
            finish(); // close activity
            overridePendingTransition(0, R.anim.slide_down); // slide-down exit
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
