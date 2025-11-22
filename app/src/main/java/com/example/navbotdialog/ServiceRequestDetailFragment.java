package com.example.navbotdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class ServiceRequestDetailFragment extends Fragment {

    private TextView tvAddItems, tvPickUpLocation, tvPickUpTime, tvStatus, tvTimestamp, tvUserId;
    private ImageView ivImage;

    private ServiceRequest request;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_service_request_detail, container, false);

        tvAddItems = view.findViewById(R.id.tvAddItems);
        tvPickUpLocation = view.findViewById(R.id.tvPickUpLocation);
        tvPickUpTime = view.findViewById(R.id.tvPickUpTime);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvTimestamp = view.findViewById(R.id.tvTimestamp);
        tvUserId = view.findViewById(R.id.tvUserId);
        ivImage = view.findViewById(R.id.ivRequestImage);

        // Get the ServiceRequest passed as argument
        if (getArguments() != null) {
            request = (ServiceRequest) getArguments().getSerializable("request");
            if (request != null) {
                populateDetails(request);
            }
        }

        return view;
    }

    private void populateDetails(ServiceRequest request) {
        tvAddItems.setText(request.getAddItems() != null ? request.getAddItems() : "N/A");
        tvPickUpLocation.setText(request.getPickUpLocation() != null ? request.getPickUpLocation() : "N/A");
        tvPickUpTime.setText(request.getPickUpTime() != null ? request.getPickUpTime() : "N/A");
        tvStatus.setText(request.getStatus() != null ? request.getStatus() : "N/A");
        tvUserId.setText(request.getUserId() != null ? request.getUserId() : "N/A");

        // Format timestamp to readable date/time (optional)
        if (request.getTimestamp() > 0) {
            java.text.DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance();
            String formattedDate = dateFormat.format(request.getTimestamp());
            tvTimestamp.setText(formattedDate);
        } else {
            tvTimestamp.setText("N/A");
        }

        // Load image with Glide
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(request.getImageUrl())
                    .placeholder(R.drawable.photo_icon) // fallback image
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.photo_icon);
        }
    }
}
