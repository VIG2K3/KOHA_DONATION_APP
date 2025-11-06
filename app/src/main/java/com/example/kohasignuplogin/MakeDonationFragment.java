package com.example.kohasignuplogin;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.net.Uri;
import android.content.Intent;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MakeDonationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MakeDonationFragment extends Fragment {

    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    private static final int PICK_IMAGE_REQUEST = 1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MakeDonationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MakeDonationFragment newInstance(String param1, String param2) {
        MakeDonationFragment fragment = new MakeDonationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MakeDonationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        storageReference = FirebaseStorage.getInstance().getReference("donation_images");
        firestore = FirebaseFirestore.getInstance();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_make_donation, container, false);

        ImageView backArrow = view.findViewById(R.id.back_arrow);

        //Set click listeners
        //Back Button
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Replace MakeDonationFragment with HomeFragment
                HomeFragment homeFragment = new HomeFragment();

                //Use getActivity() to get the FragmentManager from the hosting activity
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in_left, // enter animation
                        R.anim.slide_out_right, // exit animation
                        R.anim.slide_in_left, // popEnter animation (coming back)
                        R.anim.slide_out_right//popExit animation (going back)
                        )
                        .replace(R.id.frame_layout, homeFragment).addToBackStack(null)
                        .commit();
            }
        });

        //Add Photo from Gallery
        ImageView photoIcon = view.findViewById(R.id.photo_icon);
        photoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        return view;

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Display the selected image
                ImageView photoIcon = getView().findViewById(R.id.photo_icon);
                photoIcon.setImageURI(selectedImageUri);

                // Upload image to Firebase Storage
                uploadImageToFirebase(selectedImageUri);
            }
        }
    }
    //METHODS
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) return;

        // Create a unique file name
        String fileName = System.currentTimeMillis() + ".jpg";

        StorageReference fileRef = storageReference.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after upload
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveImageUrlToFirestore(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void saveImageUrlToFirestore(String url) {
        Map<String, Object> data = new HashMap<>();
        data.put("imageUrl", url);
        data.put("timestamp", System.currentTimeMillis());

        firestore.collection("donations")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Image uploaded and URL saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }




}