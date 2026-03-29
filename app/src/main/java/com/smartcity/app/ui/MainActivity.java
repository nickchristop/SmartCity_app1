package com.smartcity.app.ui;

import com.smartcity.app.R;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase legacy test logic has been decoupled and removed.

        // Load the Map (Home) by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MapFragment())
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.page_map);

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
