package com.smartcity.app.data.repository;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * This class operates within the strict boundaries of the Model-View-ViewModel (MVVM) architecture.
 * Leveraging the Repository Pattern, the UI and ViewModel layers are strictly "Backend Agnostic."
 * They maintain zero direct references to Firebase capabilities. This decoupling allows the underlying 
 * data source to be seamlessly migrated to a REST API or Supabase without triggering 
 * cascading source rewrites across the application surface.
 */

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smartcity.app.data.model.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * Acts as the centralized Single Source of Truth for remote data operations.
 */
public class FirebaseRepository {
    private final DatabaseReference reportsRef;

    public FirebaseRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://smartcityui-12e16-default-rtdb.europe-west1.firebasedatabase.app/");
        reportsRef = db.getReference("reports");
    }

    /**
     * Pushes a new Report object up to the cloud datastore.
     * @param report The populated report object
     */
    public void submitReport(Report report) {
        String key = reportsRef.push().getKey();
        if (key != null) {
            report.setId(key);
            reportsRef.child(key).setValue(report);
        }
    }

    /**
     * Generates a reactive data stream of reports that automatically updates
     * local subscribers whenever data changes remotely.
     * @return LiveData wrapping the latest list of Report items
     */
    public LiveData<List<Report>> getReportsLiveData() {
        MutableLiveData<List<Report>> liveData = new MutableLiveData<>();
        
        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reportsList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Report report = data.getValue(Report.class);
                    if (report != null) {
                        reportsList.add(report);
                    }
                }
                liveData.postValue(reportsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // In a production app, this would pass the error state to the ViewModel.
            }
        });
        
        return liveData;
    }
}
