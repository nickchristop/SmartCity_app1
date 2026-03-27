package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Βρες το κουμπί/κάρτα μέσα στο layout του Home
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) View reportCard = view.findViewById(R.id.card_reports); // Βεβαιώσου ότι έχεις αυτό το ID στο XML

        reportCard.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.page_report);
            }
        });

        View mapCard = view.findViewById(R.id.card_map);
        if (mapCard != null) {
            mapCard.setOnClickListener(v -> {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.page_map);
                }
            });
        }

        return view;
    }
}