package com.smartcity.app.ui;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * This class operates within the strict boundaries of the Model-View-ViewModel (MVVM) architecture.
 * Leveraging the Repository Pattern, the UI and ViewModel layers are strictly "Backend Agnostic."
 * They maintain zero direct references to Firebase capabilities. This decoupling allows the underlying 
 * data source to be seamlessly migrated to a REST API or Supabase without triggering 
 * cascading source rewrites across the application surface.
 */

import android.Manifest;
import android.content.pm.PackageManager;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.MapViewModel;

import java.util.List;

/**
 * Controller class for the high-fidelity Google Maps interface, tightly 
 * coupled dynamically to the independent MapViewModel underlying standard.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapViewModel viewModel;
    private GoogleMap googleMap;
    
    // Abstracted modern permission handling logic 
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableUserLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Single Source MVVM State Management via Lifecycle-Aware scopes
        viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        // Fetch Google Maps Fragment Integration 
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Intercept FAB to render contextual Material 3 Bottom Sheet overlapping map
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
        checkLocationPermission();

        // Implementing the Observer Pattern explicitly here
        // The View acts merely as a naive projection of ViewModel State updates.
        viewModel.getReportsLiveData().observe(getViewLifecycleOwner(), this::updateMapMarkers);
    }

    /**
     * Re-renders state directly from the reactive pipeline array natively
     */
    private void updateMapMarkers(List<Report> reports) {
        if (googleMap == null) return;
        
        googleMap.clear(); // Memory flush of deprecated states 
        
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
            googleMap.setMyLocationEnabled(true);
            
            // Standardizing map centering default coordinate plane (Athens, Hellas)
            LatLng defaultLocation = new LatLng(37.9838, 23.7275);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
            
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
