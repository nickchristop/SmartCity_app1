package com.smartcity.app.ui;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * This class operates within the strict boundaries of the Model-View-ViewModel (MVVM) architecture.
 * Leveraging the Repository Pattern, the UI and ViewModel layers are strictly "Backend Agnostic."
 * They maintain zero direct references to Firebase capabilities. This decoupling allows the underlying 
 * data source to be seamlessly migrated to a REST API or Supabase without triggering 
 * cascading source rewrites across the application surface.
 */

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.MapViewModel;

import java.util.List;

public class DashboardFragment extends Fragment {

    private MapViewModel viewModel;
    private TextView tvActiveHazards;
    private TextView tvLatestUpdate;
    private LinearLayout llStatsContainer;
    private LinearLayout llEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvActiveHazards = view.findViewById(R.id.tv_active_hazards);
        tvLatestUpdate = view.findViewById(R.id.tv_latest_update);
        llStatsContainer = view.findViewById(R.id.ll_stats_container);
        llEmptyState = view.findViewById(R.id.ll_empty_state);

        View fab = view.findViewById(R.id.fab_dashboard_report);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                ReportBottomSheet bottomSheet = new ReportBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "ReportBottomSheet");
            });
        }

        viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        viewModel.getReportsLiveData().observe(getViewLifecycleOwner(), this::updateDashboardStats);

        return view;
    }

    private void updateDashboardStats(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            llStatsContainer.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            llStatsContainer.setVisibility(View.VISIBLE);

            tvActiveHazards.setText(String.valueOf(reports.size()));

            long latestTime = 0;
            for (Report r : reports) {
                if (r.getTimestamp() > latestTime) {
                    latestTime = r.getTimestamp();
                }
            }

            if (latestTime > 0) {
                String dateString = DateFormat.format("MMM dd, HH:mm", latestTime).toString();
                tvLatestUpdate.setText(dateString);
            }
        }
    }
}
