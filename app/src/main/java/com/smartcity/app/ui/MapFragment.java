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
import com.google.android.gms.location.Priority;
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
 * Map tab.
 * - Uses getCurrentLocation() for an active GPS measurement.
 * - Falls back to the user's mock location from Settings (default: Marousi 38.04169, 23.80496).
 * - All user-visible strings go through getString() for locale support.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapViewModel viewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableUserLocation();
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_location_denied),
                            Toast.LENGTH_SHORT).show();
                    fallbackLocation();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        View fab = view.findViewById(R.id.fab_add_report);
        if (fab != null) fab.setOnClickListener(v -> {
            // Check if user is logged in before allowing report
            com.google.firebase.auth.FirebaseUser fbUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser == null) {
                // Show login prompt
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.msg_login_required_title))
                        .setMessage(getString(R.string.msg_login_required_body))
                        .setPositiveButton(getString(R.string.btn_go_sign_in), (d, w) -> {
                            // Navigate to Account tab (which shows LoginFragment if not logged in)
                            com.google.android.material.bottomnavigation.BottomNavigationView nav =
                                    requireActivity().findViewById(R.id.bottom_navigation);
                            nav.setSelectedItemId(R.id.page_account);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return;
            }
            ReportBottomSheet bottomSheet = new ReportBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "ReportBottomSheet");
        });

        View fabLocate = view.findViewById(R.id.fab_locate_me);
        if (fabLocate != null) fabLocate.setOnClickListener(v -> triggerLocateMe());

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Boot to mock/fallback location from Settings
        double[] mock = SettingsFragment.getMockLocation(requireContext());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mock[0], mock[1]), 14f));

        setMapStyle();
        checkLocationPermission();
        viewModel.getReportsLiveData().observe(getViewLifecycleOwner(), this::updateMapMarkers);
    }

    private void setMapStyle() {
        if (googleMap == null) return;
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark));
        } else {
            googleMap.setMapStyle(null);
        }
    }

    private void updateMapMarkers(List<Report> reports) {
        if (googleMap == null) return;
        googleMap.clear();
        for (Report report : reports) {
            LatLng pos = new LatLng(report.getLatitude(), report.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(report.getTitle())
                    .snippet(report.getStatus()));
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableUserLocation() {
        if (googleMap == null) return;
        try {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(() -> { triggerLocateMe(); return true; });
            triggerLocateMe();
        } catch (SecurityException e) {
            fallbackLocation();
        }
    }

    /** Uses getCurrentLocation for a fresh GPS fix; falls back to mock location */
    private void triggerLocateMe() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null && !isGoogleHQ(location.getLatitude())) {
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));
                    } else {
                        // GPS unavailable or returned emulator default
                        com.google.android.material.snackbar.Snackbar.make(
                                requireView(), getString(R.string.msg_enable_gps),
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
                        fallbackLocation();
                    }
                })
                .addOnFailureListener(e -> fallbackLocation());
    }

    /** Detect emulator's Google HQ default (~37.42°N) */
    private boolean isGoogleHQ(double lat) {
        return Math.abs(lat - 37.4220) < 0.01;
    }

    private void fallbackLocation() {
        if (googleMap == null) return;
        double[] mock = SettingsFragment.getMockLocation(requireContext());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mock[0], mock[1]), 14f));
    }
}
