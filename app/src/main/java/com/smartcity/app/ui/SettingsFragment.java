package com.smartcity.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.smartcity.app.R;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * Implements strict "Data Persistence" handling configurations locally.
 * Preserves the active user session visual theme without polluting View variables.
 */
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME = "SmartCityPrefs";
    public static final String THEME_KEY = "ThemeMode";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        RadioGroup rgTheme = view.findViewById(R.id.rg_theme);
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int currentTheme = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.rb_theme_light);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.rb_theme_dark);
        } else {
            rgTheme.check(R.id.rb_theme_system);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.rb_theme_light) mode = AppCompatDelegate.MODE_NIGHT_NO;
            if (checkedId == R.id.rb_theme_dark) mode = AppCompatDelegate.MODE_NIGHT_YES;
            
            // Atomically execute persistent local disk mutation
            prefs.edit().putInt(THEME_KEY, mode).apply();
            
            // Engage the framework dynamic shift
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        return view;
    }
}
