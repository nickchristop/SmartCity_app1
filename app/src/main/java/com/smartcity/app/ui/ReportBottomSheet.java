package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.MapViewModel;

/**
 * Material 3 Bottom Sheet serving as the contextual reporting interface.
 */
public class ReportBottomSheet extends BottomSheetDialogFragment {
    private MapViewModel viewModel;
    
    // In a full implementation, these would be fetched dynamically from user GPS
    private final double MOCK_LAT = 37.9838;
    private final double MOCK_LNG = 23.7275;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        
        // Scope ViewModel to activity to share same instance with MapFragment
        viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        EditText titleInput = view.findViewById(R.id.edit_report_title);
        EditText descInput = view.findViewById(R.id.edit_report_desc);
        View submitBtn = view.findViewById(R.id.btn_submit_report);

        if (submitBtn != null) {
            submitBtn.setOnClickListener(v -> {
                String title = titleInput.getText().toString().trim();
                String desc = descInput.getText().toString().trim();

                if (!title.isEmpty()) {
                    Report report = new Report(
                            "", 
                            "mock_user_123", 
                            title, 
                            desc, 
                            "Pending", 
                            MOCK_LAT, 
                            MOCK_LNG, 
                            System.currentTimeMillis()
                    );
                    viewModel.submitReport(report);
                    dismiss(); // Automatically slides down upon submission
                }
            });
        }
        return view;
    }
}
