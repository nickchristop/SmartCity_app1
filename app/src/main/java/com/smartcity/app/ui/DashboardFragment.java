package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Legacy stub — no longer routed to. Navigation now uses HazardsFragment for page_dashboard.
 * Kept to avoid removing the source file entirely; does nothing.
 */
public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Redirect to HazardsFragment if somehow reached
        return new HazardsFragment().onCreateView(inflater, container, savedInstanceState);
    }
}
