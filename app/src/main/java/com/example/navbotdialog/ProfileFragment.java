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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private EditText etUsername, etEmail, etNumber;
    private Button btnSave;
    private ImageView profileImage, changePhotoBtn;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 🔹 Bind UI
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etNumber = view.findViewById(R.id.etNumber);
        btnSave = view.findViewById(R.id.btnSave);
        profileImage = view.findViewById(R.id.profileImage);
        changePhotoBtn = view.findViewById(R.id.changePhotoBtn);

        // 🔹 Firebase setup
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getActivity(), "Please log in first", Toast.LENGTH_SHORT).show();
            return view;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + uid + ".jpg");

        // 🔹 Load existing user data
        loadUserProfile();

        // 🔹 Save button
        btnSave.setOnClickListener(v -> saveProfile());

        // 🔹 Change photo
        changePhotoBtn.setOnClickListener(v -> openFileChooser());

        return view;
    }

    // 🔹 Load user info from Firebase
    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    etUsername.setText(username);
                    etEmail.setText(email);
                    etNumber.setText(phone);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .into(profileImage);
                    }
                } else {
                    etEmail.setText(currentUser.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded())
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Open image picker
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // 🔹 Handle chosen image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    // 🔹 Upload profile image to Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri == null) return;

        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    userRef.child("imageUrl").setValue(imageUrl)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to update image URL", Toast.LENGTH_SHORT).show());
                })
        ).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 🔹 Save text info to Firebase Database
    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etNumber.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Name and Email required", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("username").setValue(username);
        userRef.child("email").setValue(email);
        userRef.child("phone").setValue(phone)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
