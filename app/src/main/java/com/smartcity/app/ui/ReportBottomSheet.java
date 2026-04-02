package com.smartcity.app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
 * - Location: uses getCurrentLocation() for a fresh GPS fix (not stale cached)
 * - Images: dialog lets user choose Camera or Gallery; max 5 images enforced
 * - Submitter name: pulled from AuthViewModel's user profile
 */
public class ReportBottomSheet extends BottomSheetDialogFragment {

    private MapViewModel  mapViewModel;
    private AuthViewModel authViewModel;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLat = 0.0, currentLng = 0.0;
    private final List<Uri> selectedImages = new ArrayList<>();

    // URI for the current camera capture destination
    private Uri cameraImageUri;

    // --- Activity Result Launchers (must be registered in onCreate) ---

    /** Gallery multi-select */
    private ActivityResultLauncher<String> galleryLauncher;

    /** Camera single capture */
    private ActivityResultLauncher<Uri> cameraLauncher;

    /** Permission request for camera before capture */
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gallery: pick multiple images, enforce max 5
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(), uris -> {
                    if (uris == null || uris.isEmpty()) return;
                    int remaining = 5 - selectedImages.size();
                    if (remaining <= 0) {
                        Toast.makeText(getContext(), "Already have 5 images.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int added = 0;
                    for (Uri u : uris) {
                        if (added >= remaining) break;
                        selectedImages.add(u);
                        added++;
                    }
                    if (uris.size() > remaining)
                        Toast.makeText(getContext(),
                                "Limited to 5 images total. Added " + added + ".", Toast.LENGTH_SHORT).show();
                    updateImageCount();
                });

        // Camera: capture single photo and add to list
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success && cameraImageUri != null && selectedImages.size() < 5) {
                        selectedImages.add(cameraImageUri);
                        updateImageCount();
                    } else if (selectedImages.size() >= 5) {
                        Toast.makeText(getContext(), "Maximum 5 images already selected.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Camera permission flow
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) launchCamera();
                    else Toast.makeText(getContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show();
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

        TextInputLayout   tilTitle      = view.findViewById(R.id.til_report_title);
        TextInputEditText etTitle       = view.findViewById(R.id.edit_report_title);
        TextInputEditText etDesc        = view.findViewById(R.id.edit_report_desc);
        TextView tvLocationStatus       = view.findViewById(R.id.tv_location_status);
        View btnGetLocation             = view.findViewById(R.id.btn_get_location);
        View btnAddImages               = view.findViewById(R.id.btn_add_images);
        View btnSubmit                  = view.findViewById(R.id.btn_submit_report);

        // Auto-fetch a fresh location the moment the form opens
        fetchFreshLocation(tvLocationStatus);

        btnGetLocation.setOnClickListener(v -> fetchFreshLocation(tvLocationStatus));

        // Image source dialog: Camera or Gallery
        btnAddImages.setOnClickListener(v -> showImageSourceDialog());

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText() != null
                    ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                tilTitle.setError(getString(R.string.err_title_empty));
                return;
            }
            tilTitle.setError(null);

            String desc = etDesc.getText() != null
                    ? etDesc.getText().toString().trim() : "";

            // Get submitter details from current session
            String userId       = "anonymous";
            String submitterName = "Anonymous";
            com.google.firebase.auth.FirebaseUser fbUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                userId = fbUser.getUid();
                // Use saved name from profile if available
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
            Toast.makeText(getContext(), "Report submitted!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    /**
     * Uses getCurrentLocation() which actively triggers a GPS measurement.
     * Unlike getLastLocation() this works even if there is no cached fix.
     */
    private void fetchFreshLocation(TextView tvStatus) {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvStatus.setText(getString(R.string.msg_location_unavailable));
            return;
        }
        tvStatus.setText(getString(R.string.msg_location_fetching));

        // getCurrentLocation forces a new measurement (fixes null from getLastLocation)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        tvStatus.setText(
                                String.format(Locale.US, "📍 %.5f, %.5f", currentLat, currentLng));
                    } else {
                        tvStatus.setText(getString(R.string.msg_location_unavailable));
                        Toast.makeText(getContext(),
                                getString(R.string.msg_gps_disabled), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        tvStatus.setText(getString(R.string.msg_location_unavailable)));
    }

    /** Dialog: choose between Camera and Gallery */
    private void showImageSourceDialog() {
        if (selectedImages.size() >= 5) {
            Toast.makeText(getContext(), "Maximum 5 images already selected.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Image")
                .setItems(new String[]{"Take Photo (Camera)", "Choose from Gallery"}, (dialog, which) -> {
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

    /** Creates a temp file and launches the camera to capture into it */
    private void launchCamera() {
        try {
            File photoFile = createTempImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Could not open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Creates a uniquely named file in the external cache directory */
    private File createTempImageFile() throws IOException {
        String ts   = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dir    = requireContext().getExternalCacheDir();
        return File.createTempFile("IMG_" + ts + "_", ".jpg", dir);
    }

    /** Updates the image count TextView */
    private void updateImageCount() {
        View v = getView();
        if (v == null) return;
        TextView tv = v.findViewById(R.id.tv_image_count);
        tv.setText(selectedImages.size() + " / 5 image(s) selected");
    }
}
