package com.smartcity.app.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smartcity.app.data.model.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for remote data operations.
 * Handles both Realtime Database (reports) and Storage (images).
 */
public class FirebaseRepository {

    private final DatabaseReference reportsRef;

    public FirebaseRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://smartcityui-12e16-default-rtdb.europe-west1.firebasedatabase.app/");
        reportsRef = db.getReference("reports");
    }

    /**
     * Uploads a list of image URIs to Firebase Storage under reports/{reportId}/.
     * Calls back with the list of download URLs when all uploads finish.
     * On partial failure the upload continues so the report is not blocked.
     */
    public void uploadImages(String reportId, List<Uri> imageUris, OnImagesUploadedCallback callback) {
        if (imageUris == null || imageUris.isEmpty()) {
            callback.onComplete(new ArrayList<>());
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        List<String> downloadUrls = new ArrayList<>();
        final int[] done = {0};
        final int total = imageUris.size();

        for (int i = 0; i < total; i++) {
            Uri uri = imageUris.get(i);
            StorageReference ref = storage.getReference()
                    .child("reports/" + reportId + "/image_" + i + ".jpg");

            ref.putFile(uri)
                    .continueWithTask(task -> ref.getDownloadUrl())
                    .addOnSuccessListener(dlUri -> {
                        downloadUrls.add(dlUri.toString());
                        done[0]++;
                        if (done[0] == total) callback.onComplete(downloadUrls);
                    })
                    .addOnFailureListener(e -> {
                        // Skip failed images; still return what succeeded
                        done[0]++;
                        if (done[0] == total) callback.onComplete(downloadUrls);
                    });
        }
    }

    /** Saves a fully-populated Report (with imageUrls) to the Realtime Database */
    public void submitReport(Report report) {
        String key = reportsRef.push().getKey();
        if (key != null) {
            report.setId(key);
            reportsRef.child(key).setValue(report);
        }
    }

    /**
     * Returns a reactive LiveData stream of all reports.
     * Automatically updates subscribers when Firebase data changes.
     */
    public LiveData<List<Report>> getReportsLiveData() {
        MutableLiveData<List<Report>> liveData = new MutableLiveData<>();

        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Report r = data.getValue(Report.class);
                    if (r != null) list.add(r);
                }
                liveData.postValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Post empty list on error so UI shows empty state, not a hang
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    /** Callback fired when all image uploads have completed (or been skipped) */
    public interface OnImagesUploadedCallback {
        void onComplete(List<String> downloadUrls);
    }
}
