package com.example.navbotdialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class HomeFragment extends Fragment {

    private TextView koPointsValue;
    private ImageView qrCodeImage;
    private DatabaseReference userPointsRef;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button donationCategoryButton = view.findViewById(R.id.donation_category_button);
        Button makeDonationButton = view.findViewById(R.id.make_donation_button);
        koPointsValue = view.findViewById(R.id.ko_points_value);
        qrCodeImage = view.findViewById(R.id.qrCodeImage); // Add ImageView in XML

        uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            // 🔹 Connect to your secondary database explicitly
            userPointsRef = FirebaseDatabase.getInstance(
                            "https://koha-user-points.asia-southeast1.firebasedatabase.app/"
                    )
                    .getReference("users")
                    .child(uid)
                    .child("points");

            // 🔹 Listen for live KO points
            userPointsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Object val = snapshot.getValue();
                    long points = 0;

                    if (val instanceof Long) points = (Long) val;
                    else if (val instanceof Integer) points = ((Integer) val).longValue();
                    else if (val instanceof String) {
                        try { points = Long.parseLong((String) val); }
                        catch (NumberFormatException ignored) {}
                    }

                    koPointsValue.setText(String.valueOf(points));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    koPointsValue.setText("0");
                    Log.e("HomeFragment", "Firebase error: " + error.getMessage());
                }
            });

            // 🔹 Generate QR code from UID
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(uid, BarcodeFormat.QR_CODE, 400, 400);
                qrCodeImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("HomeFragment", "QR generation error: " + e.getMessage());
            }

        } else {
            Log.e("HomeFragment", "UID is null, user not logged in");
        }

        donationCategoryButton.setOnClickListener(v -> swapFragment(new DonationCategoriesFragment()));
        makeDonationButton.setOnClickListener(v -> swapFragment(new MakeDonationFragment()));
    }

    // Helper method to swap fragments with animation
    private void swapFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );

        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
