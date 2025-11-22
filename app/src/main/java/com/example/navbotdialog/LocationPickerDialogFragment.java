package com.example.navbotdialog;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Locale;

public class LocationPickerDialogFragment extends DialogFragment {

    private MapView mapView;
    private Marker marker;
    private EditText searchEditText;
    private Button searchButton, confirmButton, cancelButton;

    private GeoPoint selectedPoint;
    private String selectedAddress = "";

    private static final String ARG_INITIAL_LAT = "initial_lat";
    private static final String ARG_INITIAL_LNG = "initial_lng";

    public static LocationPickerDialogFragment newInstance(double lat, double lng) {
        LocationPickerDialogFragment fragment = new LocationPickerDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_INITIAL_LAT, lat);
        args.putDouble(ARG_INITIAL_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        View view = inflater.inflate(R.layout.fragment_location_picker_dialog, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        confirmButton = view.findViewById(R.id.confirmButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        mapView = view.findViewById(R.id.mapView);

        // Setup mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        double lat = 5.360;  // Default latitude (Penang center)
        double lng = 100.300; // Default longitude

        if (getArguments() != null) {
            lat = getArguments().getDouble(ARG_INITIAL_LAT, lat);
            lng = getArguments().getDouble(ARG_INITIAL_LNG, lng);
        }

        selectedPoint = new GeoPoint(lat, lng);

        mapView.getController().setZoom(15);
        mapView.getController().setCenter(selectedPoint);

        // Add draggable marker
        marker = new Marker(mapView);
        marker.setPosition(selectedPoint);
        marker.setDraggable(true);
        marker.setTitle("Selected Location");
        marker.setSnippet("Loading address...");
        mapView.getOverlays().add(marker);
        marker.showInfoWindow();

        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // no-op
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // no-op
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedPoint = marker.getPosition();
                // Show "Loading address..." while fetching
                marker.setSnippet("Loading address...");
                marker.showInfoWindow();
                mapView.invalidate();
                reverseGeocode(selectedPoint);
            }
        });

        searchButton.setOnClickListener(v -> searchLocation());

        confirmButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putDouble("lat", selectedPoint.getLatitude());
            result.putDouble("lng", selectedPoint.getLongitude());
            result.putString("address", selectedAddress != null ? selectedAddress : "");
            getParentFragmentManager().setFragmentResult("locationPicked", result);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        // Initial reverse geocode to get address for starting point
        reverseGeocode(selectedPoint);

        return view;
    }

    private void searchLocation() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                    String fullAddress = address.getAddressLine(0);

                    requireActivity().runOnUiThread(() -> {
                        selectedPoint = point;
                        selectedAddress = fullAddress;
                        mapView.getController().setCenter(point);
                        marker.setPosition(point);
                        marker.setTitle("Selected Location");
                        marker.setSnippet(selectedAddress);
                        marker.showInfoWindow();
                        mapView.invalidate();
                        Toast.makeText(getContext(), "Location found: " + fullAddress, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error searching location", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void reverseGeocode(GeoPoint point) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    selectedAddress = addresses.get(0).getAddressLine(0);
                } else {
                    selectedAddress = "No address found";
                }
            } catch (Exception e) {
                e.printStackTrace();
                selectedAddress = "No address found";
            }

            // Update the marker snippet and show info window on UI thread
            requireActivity().runOnUiThread(() -> {
                marker.setSnippet(selectedAddress);
                marker.showInfoWindow();
                mapView.invalidate();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }
}
