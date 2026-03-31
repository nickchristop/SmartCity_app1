package com.smartcity.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.MapViewModel;

import java.util.List;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * Controller class for the high-fidelity Google Maps interface, tightly 
 * coupled dynamically to the independent MapViewModel standard.
 * Strictly separates SDK dependencies (Maps/Location) from logical states.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapViewModel viewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    
    // Non-fatal graceful degradation block ensuring no crashes upon 'Deny'
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableUserLocation();
                } else {
                    Toast.makeText(getContext(), "Location denied. Centering on default zone.", Toast.LENGTH_SHORT).show();
                    fallbackLocation(); // Guarantee app longevity even without tracking
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        View fab = view.findViewById(R.id.fab_add_report);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                ReportBottomSheet bottomSheet = new ReportBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "ReportBottomSheet");
            });
        }
        
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        setMapStyle(); // Dynamic Theme Shifting Execution
        checkLocationPermission();

        viewModel.getReportsLiveData().observe(getViewLifecycleOwner(), this::updateMapMarkers);
    }
    
    private void setMapStyle() {
        if (googleMap == null) return;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark));
        } else {
            googleMap.setMapStyle(null);
        }
    }

    private void updateMapMarkers(List<Report> reports) {
        if (googleMap == null) return;
        googleMap.clear(); 
        
        for (Report report : reports) {
            LatLng position = new LatLng(report.getLatitude(), report.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(report.getTitle())
                    .snippet(report.getStatus()));
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableUserLocation() {
        if (googleMap == null) return;
        try {
            // Enables the SDK floating UI crosshair
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            
            // Intercept the native click event to forcefully route through FusedLocationProvider
            googleMap.setOnMyLocationButtonClickListener(() -> {
                triggerLocateMe();
                return true; // Consume event to prevent redundant map tracking
            });
            
            triggerLocateMe(); // Boot configuration anchor
        } catch (SecurityException e) {
            fallbackLocation();
        }
    }

    private void triggerLocateMe() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    // 15f zoom smoothly centers the UI immediately onto coordinates
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                } else {
                    com.google.android.material.snackbar.Snackbar.make(requireView(), "Please turn on GPS / Location Services", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                }
            });
            
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fallbackLocation() {
        if (googleMap == null) return;
        LatLng defaultLocation = new LatLng(37.9838, 23.7275);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
    }
}
