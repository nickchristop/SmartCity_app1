package com.smartcity.app.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartcity.app.R;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * The MainActivity controls baseline fragment routing and configuration checks.
 * Bootstrapping 'SharedPreferences' natively here intercepts "White-Flash" 
 * UI issues during Dark Mode toggle states.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Theme Interception Algorithm: Forces UI recalculations before Android attaches context windows
        android.content.SharedPreferences prefs = getSharedPreferences("SmartCityPrefs", android.content.Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt("ThemeMode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(savedTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // Load the Dashboard (Landing Context) by default ONLY if fresh launch
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        bottomNav.setOnItemSelectedListener(item -> {
            androidx.fragment.app.Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.page_map) {
                selectedFragment = new MapFragment();
            } else if (itemId == R.id.page_account) {
                selectedFragment = new AccountFragment();
            } else if (itemId == R.id.page_settings) {
                selectedFragment = new SettingsFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}
