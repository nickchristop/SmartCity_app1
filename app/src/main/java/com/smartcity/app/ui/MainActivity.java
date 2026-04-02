package com.smartcity.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartcity.app.R;

/**
 * Single-Activity host.
 * - Applies persisted locale via SettingsFragment.applyLocale() on every attach.
 * - Applies persisted theme before super.onCreate() to prevent white-flash.
 * - Routes bottom-nav clicks to the correct fragment.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        // Apply saved locale so every view config uses the correct language
        super.attachBaseContext(SettingsFragment.applyLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved dark/light theme before UI attaches to avoid white-flash
        android.content.SharedPreferences prefs =
                getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt(SettingsFragment.THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default fragment on first launch is the Map tab
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = getFragment(item);
            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }
            return true;
        });
    }

    /** Maps each bottom nav item to its Fragment */
    @Nullable
    private static Fragment getFragment(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.page_map)       return new MapFragment();
        if (id == R.id.page_dashboard) return new HazardsFragment(); // Hazards tab
        if (id == R.id.page_account)   return new AccountFragment();
        if (id == R.id.page_settings)  return new SettingsFragment();
        return null;
    }
}
