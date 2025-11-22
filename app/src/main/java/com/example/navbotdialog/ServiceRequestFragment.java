package com.example.navbotdialog;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceRequestAdapter adapter;
    private List<ServiceRequest> list = new ArrayList<>();
    private DatabaseReference ref;
    private TextView textNoData;
    private String userID;
    private ValueEventListener refListener;
    private String currentFilter = "All";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_service_request, container, false);

        recyclerView = view.findViewById(R.id.recyclerRequests);
        textNoData = view.findViewById(R.id.textNoData);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            textNoData.setVisibility(View.VISIBLE);
            textNoData.setText("Please log in to view your service requests.");
            return view;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://koha-signup-login-default-rtdb.asia-southeast1.firebasedatabase.app/");
        ref = database.getReference("donations");

        adapter = new ServiceRequestAdapter(list, request -> {
            ServiceRequestDetailFragment detailFragment = new ServiceRequestDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("request", request);
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        adapter.setOnSelectionChangedListener(this::updateToolbarSelectionMode);

        recyclerView.setAdapter(adapter);

        loadRequests(currentFilter);

        return view;
    }

    private void loadRequests(String status) {
        if (userID == null) return;

        currentFilter = status;

        if (refListener != null) ref.removeEventListener(refListener);

        refListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists()) {
                    textNoData.setVisibility(View.VISIBLE);
                    textNoData.setText("No service requests found.");
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot data : snapshot.getChildren()) {
                    ServiceRequest request = data.getValue(ServiceRequest.class);
                    if (request == null) continue;

                    // Save the Firebase key for deletion
                    request.setKey(data.getKey());

                    if (userID.equals(request.getUserId()) &&
                            (status.equals("All") || status.equalsIgnoreCase(request.getStatus()))) {
                        list.add(request);
                    }
                }

                adapter.notifyDataSetChanged();
                textNoData.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textNoData.setVisibility(View.VISIBLE);
                textNoData.setText("Error loading data.");
            }
        };

        ref.addValueEventListener(refListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ref != null && refListener != null) {
            ref.removeEventListener(refListener);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_service_request, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (adapter == null) return;

        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        MenuItem filterItem = menu.findItem(R.id.action_filter);

        int selectedCount = adapter.getSelectedPositions().size();

        if (selectedCount > 0) {
            deleteItem.setVisible(true);
            filterItem.setVisible(false);
        } else {
            deleteItem.setVisible(false);
            filterItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            Set<Integer> selected = adapter.getSelectedPositions();
            for (Integer pos : selected) {
                if (pos < list.size()) {
                    ServiceRequest r = list.get(pos);
                    if (r != null && r.getKey() != null) {
                        ref.child(r.getKey()).removeValue(); // delete correctly
                    }
                }
            }
            adapter.removeSelectedItems();
            updateToolbarSelectionMode(0);
            return true;
        } else if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateToolbarSelectionMode(int selectedCount) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            if (selectedCount > 0) {
                activity.getSupportActionBar().setTitle(selectedCount + " selected");
            } else {
                activity.getSupportActionBar().setTitle("Service Requests");
            }
            activity.invalidateOptionsMenu();
        }
    }

    private void showFilterDialog() {
        String[] options = {"All", "Pending", "Approved", "Completed", "Rejected"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filter by status")
                .setItems(options, (dialog, which) -> loadRequests(options[which]))
                .show();
    }
}

