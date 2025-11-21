package com.example.navbotdialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView profileImage;
    private EditText usernameField, phoneField, emailField;
    private Button updateButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private Uri selectedImageUri;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize fields
        profileImage = view.findViewById(R.id.profileImage);
        usernameField = view.findViewById(R.id.etUsername);
        phoneField = view.findViewById(R.id.etNumber);
        emailField = view.findViewById(R.id.etEmail);
        emailField.setEnabled(false); // Email cannot be edited
        updateButton = view.findViewById(R.id.btnSave);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        loadUserData();

        profileImage.setOnClickListener(v -> chooseImage());
        updateButton.setOnClickListener(v -> updateUserData());

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        firestore.collection("Users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        usernameField.setText(document.getString("username"));
                        phoneField.setText(document.getString("phone"));
                        emailField.setText(user.getEmail()); // always show email

                        String imageUrl = document.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
        }
    }

    private void updateUserData() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();

        String username = usernameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        // 🔹 Validate username and phone
        if (TextUtils.isEmpty(username)) {
            usernameField.setError("Username cannot be empty");
            usernameField.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneField.setError("Phone number cannot be empty");
            phoneField.requestFocus();
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("phone", phone);

        firestore.collection("Users").document(uid)
                .set(map, SetOptions.merge())
                .addOnSuccessListener(a -> {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // 🔹 Refresh drawer header immediately
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshDrawerHeader();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                );

        // Upload profile image if selected
        if (selectedImageUri != null) {
            uploadProfileImage(uid);
        }
    }

    private void uploadProfileImage(String uid) {
        if (selectedImageUri == null) return;

        StorageReference ref = storage.getReference("profile_images/" + uid + "/profile.jpg");

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            firestore.collection("Users")
                                    .document(uid)
                                    .update("profileImageUrl", uri.toString())
                                    .addOnSuccessListener(a -> {
                                        Toast.makeText(getContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                                        // 🔹 Update drawer header immediately
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).refreshDrawerHeader();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to save image URL", Toast.LENGTH_SHORT).show());
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
