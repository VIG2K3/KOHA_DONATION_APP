package com.example.navbotdialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import android.text.TextUtils;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MakeDonationFragment extends Fragment {

    private static final String TAG = "MakeDonation";

    // Categories for multi-select dialog
    private String[] categories = {
            "Clothing", "Food & Groceries", "Household",
            "Education", "Electronics", "Family Care",
            "Health and Hygiene"
    };

    boolean[] selectedCategories;
    ArrayList<String> selectedList = new ArrayList<>();

    // Location picked from map
    private double pickedLat = 0;
    private double pickedLng = 0;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    private ImageView photoIcon;
    private Uri uploadedImageUri = null;

    private ActivityResultLauncher<String> pickImageLauncher;

    private EditText addItemsEditText, pickUpLocationEditText, pickUpTimeEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // Point directly to "donations" node in Realtime Database
        databaseRef = FirebaseDatabase
                .getInstance("https://koha-signup-login-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("donations");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_make_donation, container, false);

        photoIcon = view.findViewById(R.id.photo_icon);

        addItemsEditText = view.findViewById(R.id.add_items);
        pickUpLocationEditText = view.findViewById(R.id.PickUp_Location);
        pickUpTimeEditText = view.findViewById(R.id.PickUp_Time);

        selectedCategories = new boolean[categories.length];

        // Multi-select categories dialog
        addItemsEditText.setFocusable(false);
        addItemsEditText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Donation Categories");

            builder.setMultiChoiceItems(categories, selectedCategories, (dialog, index, checked) -> {
                if (checked) {
                    if (!selectedList.contains(categories[index])) {
                        selectedList.add(categories[index]);
                    }
                } else {
                    selectedList.remove(categories[index]);
                }
            });

            builder.setPositiveButton("Done", (dialog, which) -> {
                addItemsEditText.setText(TextUtils.join(", ", selectedList));
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // Listen for location results from LocationFragment ("locationResult")
        getParentFragmentManager().setFragmentResultListener(
                "locationResult",
                this,
                (requestKey, result) -> {
                    if (result != null) {
                        String address = result.getString("selectedAddress");
                        pickedLat = result.containsKey("lat") ? result.getDouble("lat") : 0;
                        pickedLng = result.containsKey("lng") ? result.getDouble("lng") : 0;
                        pickUpLocationEditText.setText(address);
                    }
                });

        // Listen for location results from LocationPickerDialogFragment ("locationPicked")
        getParentFragmentManager().setFragmentResultListener(
                "locationPicked",
                this,
                (requestKey, result) -> {
                    if (result != null) {
                        String address = result.getString("address");
                        pickedLat = result.getDouble("lat", 0);
                        pickedLng = result.getDouble("lng", 0);
                        pickUpLocationEditText.setText(address);
                    }
                });

        // Open LocationPickerDialogFragment dialog on pickUpLocationEditText click
        pickUpLocationEditText.setFocusable(false);
        pickUpLocationEditText.setOnClickListener(v -> {
            LocationPickerDialogFragment locationPickerDialog =
                    LocationPickerDialogFragment.newInstance(pickedLat, pickedLng);
            locationPickerDialog.show(getParentFragmentManager(), "locationPicker");
        });

        // Pick-up time Date + Time picker
        pickUpTimeEditText.setFocusable(false);
        pickUpTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    R.style.CustomDatePickerDialogTheme,  // Use custom orange theme here
                    (view1, year, month, day) -> {
                        calendar.set(year, month, day);

                        TimePickerDialog timePicker = new TimePickerDialog(
                                getContext(),
                                R.style.CustomTimePickerDialogTheme,  // Use custom orange theme here
                                (timeView, hour, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                                    calendar.set(Calendar.MINUTE, minute);

                                    SimpleDateFormat sdf =
                                            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                                    pickUpTimeEditText.setText(sdf.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                        );
                        timePicker.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();

        });

        // Back button
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Glide.with(requireContext()).load(uri).into(photoIcon);
                        uploadImageToFirebase(uri);
                    }
                });

        photoIcon.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Submit button
        Button submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> submitDonation());

        return view;
    }

    // Upload image to Firebase Storage
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

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            uploadedImageUri = downloadUri;
                            Glide.with(requireContext()).load(downloadUri).into(photoIcon);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Submit donation to Realtime Database
    private void submitDonation() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadedImageUri == null) {
            Toast.makeText(getContext(), "Please upload a photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check internet connection
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

        // Check if location picked from map
        if (pickedLat == 0 || pickedLng == 0) {
            Toast.makeText(getContext(), "Please select a pick-up location on the map.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare donation data map
        Map<String, Object> donationData = new HashMap<>();
        donationData.put("imageUrl", uploadedImageUri.toString());
        donationData.put("userId", currentUser.getUid());
        donationData.put("addItems", addItems);
        donationData.put("pickUpLocation", pickUpLocation);
        donationData.put("pickUpLat", pickedLat);
        donationData.put("pickUpLng", pickedLng);
        donationData.put("pickUpTime", pickUpTime);
        donationData.put("timestamp", System.currentTimeMillis());
        donationData.put("status", "Pending");

        // Push donation to Realtime Database
        DatabaseReference newDonationRef = databaseRef.push();

        newDonationRef.setValue(donationData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Donation submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
