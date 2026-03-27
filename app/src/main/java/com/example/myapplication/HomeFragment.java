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
            // Αλλαγή Fragment μέσω του Activity
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ReportFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}