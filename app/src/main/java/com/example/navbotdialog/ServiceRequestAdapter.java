package com.example.navbotdialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceRequestAdapter extends RecyclerView.Adapter<ServiceRequestAdapter.ViewHolder> {

    private final List<ServiceRequest> requestList;
    private final Set<Integer> selectedPositions = new HashSet<>();

    // --- ITEM CLICK LISTENER ---
    public interface OnItemClickListener {
        void onItemClick(ServiceRequest request);
    }

    private final OnItemClickListener clickListener;

    // --- SELECTION COUNT LISTENER ---
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private OnSelectionChangedListener selectionListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public ServiceRequestAdapter(List<ServiceRequest> requestList, OnItemClickListener clickListener) {
        this.requestList = requestList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceRequest request = requestList.get(position);

        holder.addItems.setText(request.getAddItems());
        holder.location.setText(request.getPickUpLocation());
        holder.status.setText(request.getStatus());

        Glide.with(holder.itemView.getContext())
                .load(request.getImageUrl())
                .into(holder.image);

        boolean isSelected = selectedPositions.contains(position);

        // Highlight selected items
        holder.itemContainer.setBackgroundColor(
                isSelected ?
                        holder.itemView.getContext().getColor(R.color.orange) :
                        holder.itemView.getContext().getColor(android.R.color.white)
        );

        // Set checkbox state without triggering listener
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(isSelected);

        // Checkbox click listener
        holder.checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            toggleSelection(position);
        });

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (selectedPositions.isEmpty()) {
                // Normal mode → open detail
                if (clickListener != null) clickListener.onItemClick(request);
            } else {
                // Selection mode → toggle selection
                toggleSelection(position);
            }
        });

        // Long click to enter selection mode
        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(position);
            return true;
        });
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedPositions.size());
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    // --- ViewHolder ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView addItems, location, status;
        ImageView image;
        CheckBox checkBox;
        View itemContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            addItems = itemView.findViewById(R.id.textAddItems);
            location = itemView.findViewById(R.id.textLocation);
            status = itemView.findViewById(R.id.textStatus);
            image = itemView.findViewById(R.id.requestImage);
            checkBox = itemView.findViewById(R.id.checkBoxSelect);
            itemContainer = itemView.findViewById(R.id.itemContainer);
        }
    }

    // --- Remove all selected items safely ---
    public void removeSelectedItems() {
        List<ServiceRequest> toRemove = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos < requestList.size()) {
                toRemove.add(requestList.get(pos));
            }
        }
        requestList.removeAll(toRemove);
        selectedPositions.clear();
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged(0);
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }
}
