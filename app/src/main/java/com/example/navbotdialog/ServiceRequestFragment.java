package com.example.navbotdialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceRequestAdapter adapter;
    private List<ServiceRequest> list = new ArrayList<>();
    private DatabaseReference ref;
    private Spinner spinner;
    private TextView textNoData;
    private String userID;
    private ValueEventListener refListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_request, container, false);

        recyclerView = view.findViewById(R.id.recyclerRequests);
        spinner = view.findViewById(R.id.spinnerFilter);
        textNoData = view.findViewById(R.id.textNoData);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServiceRequestAdapter(list);
        recyclerView.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("ServiceRequest", "Current userID: " + userID);
        } else {
            Log.e("ServiceRequest", "No user is logged in!");
            textNoData.setVisibility(View.VISIBLE);
            textNoData.setText("Please log in to view your service requests.");
            return view;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://koha-signup-login-default-rtdb.asia-southeast1.firebasedatabase.app/");
        ref = database.getReference("donations");
        Log.d("ServiceRequest", "Database reference path: " + ref.toString());

        // Spinner setup
        ArrayAdapter<String> filterOptions = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Pending", "Approved", "Completed", "Rejected"});
        spinner.setAdapter(filterOptions);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View spinnerView, int position, long id) {
                String selectedStatus = spinner.getSelectedItem().toString();
                loadRequests(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Trigger initial load with "All"
        loadRequests("All");

        return view;
    }

    private void loadRequests(String status) {
        if (userID == null) return;

        // Remove previous listener to prevent duplicates
        if (refListener != null) {
            ref.removeEventListener(refListener);
        }

        Log.d("ServiceRequest", "Loading requests with filter: " + status);

        refListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ServiceRequest", "onDataChange triggered. Snapshot exists: " + snapshot.exists());
                list.clear();

                if (!snapshot.exists()) {
                    Log.d("ServiceRequest", "No data found at donations node.");
                    textNoData.setVisibility(View.VISIBLE);
                    textNoData.setText("No service requests found.");
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot data : snapshot.getChildren()) {
                    Log.d("ServiceRequest", "Child key: " + data.getKey());
                    ServiceRequest request = data.getValue(ServiceRequest.class);
                    if (request == null) {
                        Log.d("ServiceRequest", "Failed to deserialize ServiceRequest for key: " + data.getKey());
                        continue;
                    }
                    String reqUserId = request.getUserId();
                    String reqStatus = request.getStatus();

                    Log.d("ServiceRequest", "Checking request userId=" + reqUserId + ", status=" + reqStatus);

                    if (reqUserId == null) {
                        Log.d("ServiceRequest", "Skipping request with null userId");
                        continue;
                    }

                    if (status.equals("All") || (reqStatus != null && reqStatus.equalsIgnoreCase(status))) {
                        if (userID.equals(reqUserId)) {
                            list.add(request);
                            Log.d("ServiceRequest", "Added request with key: " + data.getKey());
                        } else {
                            Log.d("ServiceRequest", "Skipped due to userId mismatch");
                        }
                    } else {
                        Log.d("ServiceRequest", "Skipped due to status mismatch");
                    }
                }

                adapter.notifyDataSetChanged();

                if (list.isEmpty()) {
                    textNoData.setVisibility(View.VISIBLE);
                    textNoData.setText("No service requests found.");
                } else {
                    textNoData.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ServiceRequest", "Database error: " + error.getMessage());
                textNoData.setVisibility(View.VISIBLE);
                textNoData.setText("Error loading data.");
            }
        };
        Log.d("ServiceRequest", "Adding ValueEventListener to ref: " + ref.toString());
        ref.addValueEventListener(refListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ref != null && refListener != null) {
            ref.removeEventListener(refListener);
        }
    }
}
