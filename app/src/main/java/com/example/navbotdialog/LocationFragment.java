package com.example.navbotdialog;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocationFragment extends Fragment {

    private MapView mapView;
    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay compassOverlay;
    private final List<Shop> shops = new ArrayList<>();
    private final List<Marker> shopMarkers = new ArrayList<>();
    private Polyline currentRoute = null;
    private boolean routeVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Map setup
        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.setBuiltInZoomControls(true);

//  Enable rotation gestures
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationGestureOverlay);


        IMapController mapController = mapView.getController();

// Bounding box still for reference (Penang Island)
        BoundingBox penangBounds = new BoundingBox(
                5.480,   // north
                100.350, // east
                5.240,   // south
                100.180  // west
        );

// Set center roughly around Jelutong/Bayan Baru (midpoint between George Town & Queensbay)
        GeoPoint centerPoint = new GeoPoint(5.360, 100.300);

// Slightly closer zoom to show both halves clearly
        mapController.setZoom(11.5);
        mapController.setCenter(centerPoint);

// Optional animation if you want smooth loading
        mapView.postDelayed(() -> {
            mapController.animateTo(centerPoint);
        }, 800);

        mapView.invalidate();

        // Compass (top-left)
        compassOverlay = new CompassOverlay(requireContext(), mapView);
        compassOverlay.enableCompass();
        compassOverlay.setEnabled(true);
        mapView.getOverlays().add(compassOverlay);

        // User location
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(mLocationOverlay);

        // Sample center location data
        shops.add(new Shop("Koha Centre(1)", "Lot 31, Sunshine Mall, Jalan Thean Tek,\n11500 Air Itam, Penang", 5.39852, 100.28689, "Air Itam"));
        shops.add(new Shop("Koha Centre(2)", "Lot 100, QueensBay Mall Jalan Bayan Indah,\n11900 Bayan Lepas, Penang", 5.334255, 100.306686, "Bayan Lepas"));
        shops.add(new Shop("Koha Centre(3)", "Lot 182, 1st Avenue Mall, Jalan Magazine,\n10300 George Town, Penang", 5.41335, 100.33126, "Georgetown"));

        // Filter setup
        Spinner filterSpinner = view.findViewById(R.id.filterSpinner);
        Button filterButton = view.findViewById(R.id.filterButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.shop_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterButton.setOnClickListener(v -> {
            String selectedType = filterSpinner.getSelectedItem().toString();
            refreshMarkers(selectedType);
            focusOnMarker(selectedType); //  Move camera to that location
        });


        refreshMarkers("All");

        // Request permission if needed
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        return view;
    }

    // Refresh markers
    private void refreshMarkers(String type) {
        for (Marker marker : shopMarkers) mapView.getOverlays().remove(marker);
        shopMarkers.clear();

        for (Shop shop : shops) {
            if (type.equals("All") || shop.type.equalsIgnoreCase(type)) {
                addMarker(shop);
            }
        }

        mapView.invalidate();
    }

    private void focusOnMarker(String type) {
        IMapController mapController = mapView.getController();

        for (Shop shop : shops) {
            if (shop.type.equalsIgnoreCase(type)) {
                GeoPoint point = new GeoPoint(shop.latitude, shop.longitude);
                mapController.animateTo(point);
                mapController.setZoom(15.5); // adjust zoom level as you like
                return; // stop after first match
            }
        }

        // If "All" is selected, reset to Penang center
        if (type.equalsIgnoreCase("All")) {
            GeoPoint centerPoint = new GeoPoint(5.360, 100.300);
            mapController.animateTo(centerPoint);
            mapController.setZoom(11.5);
        }
    }

    // Add marker
    private void addMarker(Shop shop) {
        GeoPoint point = new GeoPoint(shop.latitude, shop.longitude);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setPanToView(false); // Prevent camera recentering
        marker.setDraggable(false);

        // Default marker (stable when zooming)
        Drawable drawable = getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default);

// Convert to Bitmap and resize
        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();
        int width = 120;   //  adjust size here
        int height = 200;  //  adjust size here
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
        Drawable scaledDrawable = new BitmapDrawable(getResources(), scaledBitmap);
        marker.setIcon(scaledDrawable);

        marker.setTitle(shop.name);
        marker.setSnippet(shop.address);
        marker.setInfoWindow(new MarkerInfoWindow(shop, mapView));

        marker.setOnMarkerClickListener((m, mv) -> {
            if (m.isInfoWindowOpen()) m.closeInfoWindow();
            else m.showInfoWindow();
            return true;
        });

        shopMarkers.add(marker);
        mapView.getOverlays().add(marker);
    }

    // Custom InfoWindow
    private class MarkerInfoWindow extends InfoWindow {
        private final Shop shop;

        MarkerInfoWindow(Shop shop, MapView mapView) {
            super(R.layout.gps_window, mapView);
            this.shop = shop;
        }

        @Override
        public void onOpen(Object item) {
            View v = mView;
            TextView name = v.findViewById(R.id.shopName);
            TextView address = v.findViewById(R.id.shopAddress);
            Button routeButton = v.findViewById(R.id.routeButton);
            Button copyButton = v.findViewById(R.id.copyButton);
            Button closeButton = v.findViewById(R.id.closeButton);

            name.setText(shop.name);
            address.setText(shop.address);

            routeButton.setText(routeVisible ? "Hide Route" : "Navigate");

            routeButton.setOnClickListener(btn -> {
                GeoPoint userLoc = mLocationOverlay.getMyLocation();
                if (userLoc == null) {
                    Toast.makeText(getContext(), "User location not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (routeVisible) {
                    // Hide route
                    if (currentRoute != null) mapView.getOverlays().remove(currentRoute);
                    mapView.invalidate();
                    currentRoute = null;
                    routeVisible = false;
                    routeButton.setText("Navigate");
                } else {
                    // Fetch route
                    new FetchRouteTask(userLoc, new GeoPoint(shop.latitude, shop.longitude), routeButton).execute();
                }
            });

            copyButton.setOnClickListener(btn -> {
                ClipboardManager clipboard = (ClipboardManager)
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Address", shop.address);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Address copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            closeButton.setOnClickListener(v1 -> close());
        }

        @Override
        public void onClose() {}
    }

    // OSRM route fetcher Geopoint
    private class FetchRouteTask extends AsyncTask<Void, Void, List<GeoPoint>> {
        private final GeoPoint start, end;
        private final Button toggleButton;

        FetchRouteTask(GeoPoint start, GeoPoint end, Button toggleButton) {
            this.start = start;
            this.end = end;
            this.toggleButton = toggleButton;
        }

        @Override
        protected List<GeoPoint> doInBackground(Void... voids) {
            List<GeoPoint> routePoints = new ArrayList<>();
            try {
                String urlStr = String.format(
                        "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                        start.getLongitude(), start.getLatitude(),
                        end.getLongitude(), end.getLatitude());

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray coordinates = json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    routePoints.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routePoints;
        }

        @Override
        protected void onPostExecute(List<GeoPoint> geoPoints) {
            if (!geoPoints.isEmpty()) {
                currentRoute = new Polyline();
                currentRoute.setPoints(geoPoints);
                currentRoute.setColor(Color.BLUE);
                currentRoute.setWidth(8f);
                mapView.getOverlays().add(currentRoute);
                mapView.invalidate();
                routeVisible = true;
                toggleButton.setText("Hide Route");
            } else {
                Toast.makeText(getContext(), "Unable to fetch route", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Centre model
    private static class Shop {
        String name, address, type;
        double latitude, longitude;

        Shop(String name, String address, double latitude, double longitude, String type) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        mLocationOverlay.disableMyLocation();
    }
}
