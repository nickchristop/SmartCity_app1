package com.smartcity.app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.AuthViewModel;
import com.smartcity.app.viewmodel.MapViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Report form BottomSheet.
 *
 * LOCATION:
 *  Uses getCurrentLocation(HIGH_ACCURACY) for a fresh GPS measurement.
 *  If the result is null, unreachable, or matches Google HQ (emulator default),
 *  falls back to Marousi, Greece (38.0492, 23.7969) — Plus Code 2RR8+HM.
 *
 * IMAGES:
 *  Dialog offers Camera or Gallery. Max 5 images total (combined).
 *
 * ALL user-visible strings go through getString(R.string.xxx) so Greek locale is respected.
 */
public class ReportBottomSheet extends BottomSheetDialogFragment {

    // Marousi, Greece — 2RR8+HM Marousi (fallback when GPS unavailable / emulator)
    private static final double FALLBACK_LAT = 38.04920;
    private static final double FALLBACK_LNG = 23.79690;

    // Google HQ coords (emulator default) — detect and replace
    private static final double GOOGLE_HQ_LAT = 37.4220;
    private static final double GOOGLE_HQ_DELTA = 0.01; // within 1km radius

    private MapViewModel  mapViewModel;
    private AuthViewModel authViewModel;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLat = FALLBACK_LAT;
    private double currentLng = FALLBACK_LNG;

    private final List<Uri> selectedImages = new ArrayList<>();
    private Uri cameraImageUri;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri>    cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(), uris -> {
                    if (uris == null || uris.isEmpty()) return;
                    int remaining = 5 - selectedImages.size();
                    if (remaining <= 0) {
                        Toast.makeText(getContext(), getString(R.string.msg_max_images), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int added = 0;
                    for (Uri u : uris) {
                        if (added >= remaining) break;
                        selectedImages.add(u); added++;
                    }
                    if (uris.size() > remaining)
                        Toast.makeText(getContext(), getString(R.string.msg_images_limited), Toast.LENGTH_SHORT).show();
                    updateImageCount();
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success && cameraImageUri != null && selectedImages.size() < 5) {
                        selectedImages.add(cameraImageUri);
                        updateImageCount();
                    } else if (selectedImages.size() >= 5) {
                        Toast.makeText(getContext(), getString(R.string.msg_max_images), Toast.LENGTH_SHORT).show();
                    }
                });

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) launchCamera();
                    else Toast.makeText(getContext(), getString(R.string.msg_camera_denied), Toast.LENGTH_SHORT).show();
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        mapViewModel  = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        TextInputLayout   tilTitle    = view.findViewById(R.id.til_report_title);
        TextInputEditText etTitle     = view.findViewById(R.id.edit_report_title);
        TextInputEditText etDesc      = view.findViewById(R.id.edit_report_desc);
        TextView          tvLocation  = view.findViewById(R.id.tv_location_status);
        View              btnLocation = view.findViewById(R.id.btn_get_location);
        View              btnImages   = view.findViewById(R.id.btn_add_images);
        View              btnSubmit   = view.findViewById(R.id.btn_submit_report);

        // Show Marousi as default immediately; attempt real GPS in background
        tvLocation.setText(getString(R.string.text_location_fallback));
        fetchFreshLocation(tvLocation);

        btnLocation.setOnClickListener(v -> fetchFreshLocation(tvLocation));
        btnImages.setOnClickListener(v -> showImageSourceDialog());

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                tilTitle.setError(getString(R.string.err_title_empty));
                return;
            }
            tilTitle.setError(null);
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";

            String userId        = "anonymous";
            String submitterName = getString(R.string.text_anonymous);
            com.google.firebase.auth.FirebaseUser fbUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                userId = fbUser.getUid();
                if (authViewModel.getUser().getValue() != null
                        && !authViewModel.getUser().getValue().getName().isEmpty()) {
                    submitterName = authViewModel.getUser().getValue().getName();
                } else {
                    submitterName = fbUser.getEmail();
                }
            }

            Report report = new Report("", userId, submitterName, title, desc,
                    "Pending", currentLat, currentLng, System.currentTimeMillis());
            btnSubmit.setEnabled(false);
            mapViewModel.submitReportWithImages(report, new ArrayList<>(selectedImages));
            Toast.makeText(getContext(), getString(R.string.msg_report_submitted), Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    /**
     * Requests a fresh GPS fix.
     * Falls back to Marousi if:
     *   - Permission not granted
     *   - Result is null (GPS off or emulator with no mock location)
     *   - Result matches Google HQ (emulator default ~37.422°N)
     */
    private void fetchFreshLocation(TextView tvStatus) {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            applyFallback(tvStatus);
            return;
        }
        tvStatus.setText(getString(R.string.msg_location_fetching));

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null && !isGoogleHQ(location.getLatitude())) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        tvStatus.setText(String.format(Locale.US,
                                "📍 %.5f, %.5f", currentLat, currentLng));
                    } else {
                        // GPS returned null or emulator's Google HQ default
                        applyFallback(tvStatus);
                    }
                })
                .addOnFailureListener(e -> applyFallback(tvStatus));
    }

    /** Returns true if lat is close to Google HQ (emulator mock default ~37.42°N) */
    private boolean isGoogleHQ(double lat) {
        return Math.abs(lat - GOOGLE_HQ_LAT) < GOOGLE_HQ_DELTA;
    }

    private void applyFallback(TextView tvStatus) {
        currentLat = FALLBACK_LAT;
        currentLng = FALLBACK_LNG;
        tvStatus.setText(getString(R.string.text_location_fallback));
    }

    private void showImageSourceDialog() {
        if (selectedImages.size() >= 5) {
            Toast.makeText(getContext(), getString(R.string.msg_max_images), Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.title_image_source))
                .setItems(new String[]{
                        getString(R.string.opt_camera),
                        getString(R.string.opt_gallery)
                }, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndLaunch();
                    else galleryLauncher.launch("image/*");
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createTempImageFile();
            cameraImageUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(getContext(), getString(R.string.msg_camera_error), Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempImageFile() throws IOException {
        String ts  = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File   dir = requireContext().getExternalCacheDir();
        return File.createTempFile("IMG_" + ts + "_", ".jpg", dir);
    }

    private void updateImageCount() {
        View v = getView();
        if (v == null) return;
        ((TextView) v.findViewById(R.id.tv_image_count))
                .setText(getString(R.string.fmt_image_count, selectedImages.size()));
    }
}
