package com.smartcity.app.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;
import com.smartcity.app.viewmodel.MapViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hazards tab: Active (Pending/In Progress) | Past (Resolved/Completed).
 * Tapping a hazard navigates to HazardDetailFragment.
 * Fires a local push notification when a hazard transitions to "Resolved".
 */
public class HazardsFragment extends Fragment {

    private static final String CHANNEL_ID = "hazard_updates";

    private MapViewModel viewModel;
    private HazardAdapter adapter;
    private final List<Report> activeReports = new ArrayList<>();
    private final List<Report> pastReports   = new ArrayList<>();
    private int currentTab = 0;

    // Track which IDs we already know are resolved to avoid repeat notifications
    private final Set<String> knownResolvedIds = new HashSet<>();
    private boolean isFirstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        createNotificationChannel();

        RecyclerView rv     = view.findViewById(R.id.rv_hazards);
        TextView tvEmpty    = view.findViewById(R.id.tv_hazards_empty);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_hazards);

        adapter = new HazardAdapter(new ArrayList<>(), report ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, HazardDetailFragment.newInstance(report))
                        .addToBackStack(null)
                        .commit());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

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
                    if ("Resolved".equalsIgnoreCase(r.getStatus())) {
                        pastReports.add(r);
                        // Fire notification if this is a newly resolved hazard
                        if (!isFirstLoad
                                && r.getId() != null
                                && !knownResolvedIds.contains(r.getId())) {
                            sendCompletedNotification(r);
                        }
                        if (r.getId() != null) knownResolvedIds.add(r.getId());
                    } else {
                        activeReports.add(r);
                    }
                }
            }
            isFirstLoad = false;
            refreshList(rv, tvEmpty);
        });

        return view;
    }

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

    /** Creates an Android notification channel (required API 26+) */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications when a hazard is marked as completed.");
            NotificationManager mgr = requireContext().getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(channel);
        }
    }

    /** Sends a local push notification for a newly completed hazard */
    private void sendCompletedNotification(Report report) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getString(R.string.notif_completed_title))
                .setContentText(String.format(getString(R.string.notif_completed_body), report.getTitle()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat mgr = NotificationManagerCompat.from(requireContext());
            // Use hashCode of report ID as unique notification id
            int notifId = report.getId() != null ? report.getId().hashCode() : (int) System.currentTimeMillis();
            mgr.notify(notifId, builder.build());
        } catch (SecurityException ignored) {
            // POST_NOTIFICATIONS permission not granted on API 33+ — silently skip
        }
    }
}
