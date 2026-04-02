package com.smartcity.app.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.smartcity.app.data.model.Report;
import com.smartcity.app.data.repository.FirebaseRepository;

import java.util.List;

/**
 * ViewModel for map and report data.
 * Bridges the UI layer to FirebaseRepository without direct Firebase references in the UI.
 */
public class MapViewModel extends ViewModel {

    private final FirebaseRepository repository;
    private final LiveData<List<Report>> reportsLiveData;

    public MapViewModel() {
        repository = new FirebaseRepository();
        // Cache the LiveData stream from the single source of truth
        reportsLiveData = repository.getReportsLiveData();
    }

    /** Exposes the reactive reports stream to the UI */
    public LiveData<List<Report>> getReportsLiveData() {
        return reportsLiveData;
    }

    /** Submit a plain report (no images) */
    public void submitReport(Report report) {
        repository.submitReport(report);
    }

    /**
     * Submit a report with optional images.
     * Images are uploaded to Firebase Storage first; download URLs are stored in the report.
     */
    public void submitReportWithImages(Report report, List<Uri> imageUris) {
        repository.uploadImages(report.getId().isEmpty()
                ? String.valueOf(System.currentTimeMillis()) : report.getId(),
                imageUris,
                downloadUrls -> {
                    report.setImageUrls(downloadUrls);
                    repository.submitReport(report);
                });
    }
}
