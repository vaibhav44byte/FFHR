package com.example.ffhr.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.Collections;
import java.util.Comparator;


import com.example.ffhr.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SelectFarmBoundaryActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_BOUNDARY_POINTS = "boundary_points";
    public static final String EXTRA_AREA_ACRES = "area_acres";
    public static final String EXTRA_AREA_SQ_METERS = "area_sq_meters";
    public static final String EXTRA_CENTER_LAT = "center_lat";
    public static final String EXTRA_CENTER_LON = "center_lon";
    public static final String EXTRA_ADDRESS = "address";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvBoundaryArea;
    private TextView tvBoundaryAddress;
    private TextView tvBoundaryHint;
    private Button btnUndoPoint;
    private Button btnClearBoundary;
    private Button btnFinishBoundary;

    private final List<LatLng> boundaryPoints = new ArrayList<>();
    private final List<Marker> pointMarkers = new ArrayList<>();
    private Polygon farmPolygon;

    private double areaSqMeters = 0.0;
    private double areaAcres = 0.0;
    private LatLng centerPoint;
    private String resolvedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_farm_boundary);

        tvBoundaryArea = findViewById(R.id.tvBoundaryArea);
        tvBoundaryAddress = findViewById(R.id.tvBoundaryAddress);
        tvBoundaryHint = findViewById(R.id.tvBoundaryHint);
        btnUndoPoint = findViewById(R.id.btnUndoPoint);
        btnClearBoundary = findViewById(R.id.btnClearBoundary);
        btnFinishBoundary = findViewById(R.id.btnFinishBoundary);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapBoundary);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnUndoPoint.setOnClickListener(v -> undoLastPoint());
        btnClearBoundary.setOnClickListener(v -> clearBoundary());
        btnFinishBoundary.setOnClickListener(v -> finishBoundary());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);



        enableMyLocationIfAllowed();

        googleMap.setOnMapClickListener(latLng -> {
            addBoundaryPoint(latLng);
        });
    }

    private void enableMyLocationIfAllowed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 201);
            moveToFallbackLocation();
            return;
        }

        googleMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 18f));
            } else {
                moveToFallbackLocation();
            }
        });
    }

    private void moveToFallbackLocation() {
        LatLng bhopal = new LatLng(23.2599, 77.4126);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bhopal, 16f));
    }

    private void addBoundaryPoint(LatLng latLng) {
        boundaryPoints.add(latLng);

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Point " + boundaryPoints.size()));

        if (marker != null) {
            pointMarkers.add(marker);
        }

        redrawPolygon();
    }

    private void redrawPolygon() {
        if (farmPolygon != null) {
            farmPolygon.remove();
        }

        if (boundaryPoints.size() >= 3) {
            List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(10));

            List<LatLng> orderedPoints = getOrderedBoundaryPoints();

            farmPolygon = googleMap.addPolygon(new PolygonOptions()
                    .addAll(orderedPoints)
                    .strokeColor(0xFF1E7B34)
                    .fillColor(0x331E7B34)
                    .strokeWidth(5f)
                    .strokeJointType(JointType.ROUND)
                    .strokePattern(pattern));

            areaSqMeters = SphericalUtil.computeArea(orderedPoints);
            centerPoint = getBoundsCenter(orderedPoints);
            areaAcres = areaSqMeters / 4046.8564224;


            tvBoundaryArea.setText(String.format(Locale.US,
                    "Area: %.2f acres (%.0f sqm)", areaAcres, areaSqMeters));
            tvBoundaryHint.setText("Boundary ready. You can finish or continue adjusting.");
        } else {
            areaSqMeters = 0.0;
            areaAcres = 0.0;
            centerPoint = null;
            tvBoundaryArea.setText("Area: 0.00 acres");
            tvBoundaryHint.setText("Add " + (3 - boundaryPoints.size()) + " more point(s) to create boundary.");
        }
    }

    private LatLng getBoundsCenter(List<LatLng> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        return builder.build().getCenter();
    }

    private void undoLastPoint() {
        if (boundaryPoints.isEmpty()) {
            return;
        }

        boundaryPoints.remove(boundaryPoints.size() - 1);

        if (!pointMarkers.isEmpty()) {
            Marker marker = pointMarkers.remove(pointMarkers.size() - 1);
            marker.remove();
        }

        resolvedAddress = "";
        tvBoundaryAddress.setText("Address will appear after finishing boundary");
        redrawPolygon();
    }

    private void clearBoundary() {
        boundaryPoints.clear();

        for (Marker marker : pointMarkers) {
            marker.remove();
        }
        pointMarkers.clear();

        if (farmPolygon != null) {
            farmPolygon.remove();
            farmPolygon = null;
        }

        areaSqMeters = 0.0;
        areaAcres = 0.0;
        centerPoint = null;
        resolvedAddress = "";

        tvBoundaryArea.setText("Area: 0.00 acres");
        tvBoundaryAddress.setText("Address will appear after finishing boundary");
        tvBoundaryHint.setText("Minimum 3 points required. Tap around the field edge.");
    }

    private void finishBoundary() {
        if (boundaryPoints.size() < 3 || centerPoint == null) {
            Toast.makeText(this, "Select at least 3 farm boundary points", Toast.LENGTH_SHORT).show();
            return;
        }

        resolvedAddress = reverseGeocode(centerPoint);
        tvBoundaryAddress.setText(resolvedAddress.isEmpty()
                ? "Address not available"
                : resolvedAddress);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_BOUNDARY_POINTS, serializeBoundaryPoints());
        resultIntent.putExtra(EXTRA_AREA_ACRES, areaAcres);
        resultIntent.putExtra(EXTRA_AREA_SQ_METERS, areaSqMeters);
        resultIntent.putExtra(EXTRA_CENTER_LAT, centerPoint.latitude);
        resultIntent.putExtra(EXTRA_CENTER_LON, centerPoint.longitude);
        resultIntent.putExtra(EXTRA_ADDRESS, resolvedAddress);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String serializeBoundaryPoints() {
        StringBuilder builder = new StringBuilder();

        for (LatLng point : getOrderedBoundaryPoints()) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(point.latitude).append(",").append(point.longitude);
        }

        return builder.toString();
    }

    private String reverseGeocode(LatLng point) {
        try {
            if (!Geocoder.isPresent()) {
                return "";
            }

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);

            if (addresses == null || addresses.isEmpty()) {
                return "";
            }

            Address address = addresses.get(0);
            return address.getAddressLine(0) != null ? address.getAddressLine(0) : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 201 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationIfAllowed();
        }
    }
    private List<LatLng> getOrderedBoundaryPoints() {
        List<LatLng> orderedPoints = new ArrayList<>(boundaryPoints);

        if (orderedPoints.size() < 3) {
            return orderedPoints;
        }

        double centerLat = 0.0;
        double centerLon = 0.0;

        for (LatLng point : orderedPoints) {
            centerLat += point.latitude;
            centerLon += point.longitude;
        }

        centerLat = centerLat / orderedPoints.size();
        centerLon = centerLon / orderedPoints.size();

        final double finalCenterLat = centerLat;
        final double finalCenterLon = centerLon;

        Collections.sort(orderedPoints, (p1, p2) -> {
            double angle1 = Math.atan2(p1.longitude - finalCenterLon, p1.latitude - finalCenterLat);
            double angle2 = Math.atan2(p2.longitude - finalCenterLon, p2.latitude - finalCenterLat);
            return Double.compare(angle1, angle2);
        });

        return orderedPoints;
    }

}
