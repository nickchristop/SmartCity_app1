package com.smartcity.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.smartcity.app.data.model.Report;
import com.smartcity.app.data.repository.FirebaseRepository;

import java.util.List;

/**
 * ViewModel responsible for managing Map data states and decoupling the UI
 * from direct Firebase interactions.
 */
public class MapViewModel extends ViewModel {
    private final FirebaseRepository repository;
    private final LiveData<List<Report>> reportsLiveData;

    public MapViewModel() {
        // Normally, we would use Hilt or Dagger for dependency injection here
        repository = new FirebaseRepository();
        
        // Cache the LiveData stream directly from the single source of truth
        reportsLiveData = repository.getReportsLiveData();
    }

    /**
     * Exposes the observable live data stream to the UI layer.
     */
    public LiveData<List<Report>> getReportsLiveData() {
        return reportsLiveData;
    }

    /**
     * Bridges user submission from the BottomSheet to the repository layer.
     */
    public void submitReport(Report report) {
        repository.submitReport(report);
    }
}
