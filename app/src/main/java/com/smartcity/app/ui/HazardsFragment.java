package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.MapViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Hazards tab: Active (Pending/In Progress) | Past (Resolved/Completed).
 * Tapping a hazard navigates to HazardDetailFragment.
 */
public class HazardsFragment extends Fragment {

    private MapViewModel viewModel;
    private HazardAdapter adapter;
    private final List<Report> activeReports = new ArrayList<>();
    private final List<Report> pastReports   = new ArrayList<>();
    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        RecyclerView rv     = view.findViewById(R.id.rv_hazards);
        TextView tvEmpty    = view.findViewById(R.id.tv_hazards_empty);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_hazards);

        // Click a hazard → open detail fragment (add to back stack so Back works)
        adapter = new HazardAdapter(new ArrayList<>(), report ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, HazardDetailFragment.newInstance(report))
                        .addToBackStack(null)
                        .commit());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // Active / Past tabs
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_hazards));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_past_hazards));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                refreshList(rv, tvEmpty);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        viewModel.getReportsLiveData().observe(getViewLifecycleOwner(), reports -> {
            activeReports.clear();
            pastReports.clear();
            if (reports != null) {
                for (Report r : reports) {
                    if ("Resolved".equalsIgnoreCase(r.getStatus())) pastReports.add(r);
                    else                                              activeReports.add(r);
                }
            }
            refreshList(rv, tvEmpty);
        });

        return view;
    }

    /** Swap adapter dataset based on selected tab and toggle empty state text */
    private void refreshList(RecyclerView rv, TextView tvEmpty) {
        List<Report> list = (currentTab == 0) ? activeReports : pastReports;
        adapter.setReports(list);
        if (list.isEmpty()) {
            rv.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(currentTab == 0 ? R.string.empty_hazards : R.string.empty_past_hazards);
        } else {
            rv.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
