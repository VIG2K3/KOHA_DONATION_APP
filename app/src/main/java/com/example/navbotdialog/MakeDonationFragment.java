package com.example.navbotdialog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class MakeDonationFragment extends Fragment {

    private static final String TAG = "MakeDonation";

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    private ImageView photoIcon;
    private Uri uploadedImageUri = null;

    private ActivityResultLauncher<String> pickImageLauncher;

    private EditText addItemsEditText, pickUpLocationEditText, pickUpTimeEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() CALLED");

        // Firebase Auth
        auth = FirebaseAuth.getInstance();

        // POINTS DIRECTLY to "donations"
        databaseRef = FirebaseDatabase
                .getInstance("https://koha-signup-login-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("donations");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView() CALLED");
        View view = inflater.inflate(R.layout.fragment_make_donation, container, false);

        photoIcon = view.findViewById(R.id.photo_icon);

        addItemsEditText = view.findViewById(R.id.add_items);
        pickUpLocationEditText = view.findViewById(R.id.PickUp_Location);
        pickUpTimeEditText = view.findViewById(R.id.PickUp_Time);

        // Back arrow handling
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            HomeFragment homeFragment = new HomeFragment();

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_left,    // enter animation
                            R.anim.slide_out_right,  // exit animation
                            R.anim.slide_in_left,    // pop enter animation
                            R.anim.slide_out_right   // pop exit animation
                    )
                    .replace(R.id.frame_layout, homeFragment)
                    .addToBackStack(null)
                    .commit();
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d(TAG, "Image picked: " + uri);
                        Glide.with(requireContext()).load(uri).into(photoIcon);
                        uploadImageToFirebase(uri);
                    }
                }
        );

        photoIcon.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> submitDonation());

        return view;
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String fileName = System.currentTimeMillis() + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("donation_images/" + uid + "/" + fileName);

        Log.d(TAG, "Uploading image to Firebase Storage: " + storageRef.getPath());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            uploadedImageUri = downloadUri;
                            Glide.with(requireContext()).load(downloadUri).into(photoIcon);
                            Log.d(TAG, "Image uploaded successfully, download URL: " + downloadUri);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get download URL", e);
                            Toast.makeText(getContext(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void submitDonation() {
        Log.d(TAG, "submitDonation() called");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadedImageUri == null) {
            Toast.makeText(getContext(), "Please upload a photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check network
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        String addItems = addItemsEditText.getText().toString().trim();
        String pickUpLocation = pickUpLocationEditText.getText().toString().trim();
        String pickUpTime = pickUpTimeEditText.getText().toString().trim();

        if (addItems.isEmpty() || pickUpLocation.isEmpty() || pickUpTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> donationData = new HashMap<>();
        donationData.put("imageUrl", uploadedImageUri.toString());
        donationData.put("userId", currentUser.getUid());
        donationData.put("addItems", addItems);
        donationData.put("pickUpLocation", pickUpLocation);
        donationData.put("pickUpTime", pickUpTime);
        donationData.put("timestamp", System.currentTimeMillis());
        donationData.put("status", "Pending");

        Log.d(TAG, "Submitting donation data to Realtime DB: " + donationData);

        DatabaseReference newDonationRef = databaseRef.push();

        newDonationRef.setValue(donationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Donation submitted successfully! Key: " + newDonationRef.getKey());
                    Toast.makeText(getContext(), "Donation submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to submit donation", e);
                    Toast.makeText(getContext(), "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
