package com.example.navbotdialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ServiceRequestAdapter extends RecyclerView.Adapter<ServiceRequestAdapter.ViewHolder> {

    private List<ServiceRequest> requestList;

    public ServiceRequestAdapter(List<ServiceRequest> requestList) {
        this.requestList = requestList;
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
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView addItems, location, status;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            addItems = itemView.findViewById(R.id.textAddItems);
            location = itemView.findViewById(R.id.textLocation);
            status = itemView.findViewById(R.id.textStatus);
            image = itemView.findViewById(R.id.requestImage);
        }
    }
}