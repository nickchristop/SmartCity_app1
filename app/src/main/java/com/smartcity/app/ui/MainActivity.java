package com.smartcity.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartcity.app.R;
import com.smartcity.app.viewmodel.AuthViewModel;

/**
 * Single-Activity host.
 * - Applies persisted locale via SettingsFragment.applyLocale() on every attach.
 * - Applies persisted theme before super.onCreate() to prevent white-flash.
 * - Routes bottom-nav clicks to the correct fragment.
 * - Shows/hides sign-out icon in toolbar based on auth state.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
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

        // --- Toolbar sign-out icon ---
        ImageButton btnToolbarSignout = findViewById(R.id.btn_toolbar_signout);
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Show sign-out icon only when logged in
        authViewModel.getUser().observe(this, user -> {
            if (user != null) {
                btnToolbarSignout.setVisibility(View.VISIBLE);
            } else {
                btnToolbarSignout.setVisibility(View.GONE);
            }
        });
        btnToolbarSignout.setOnClickListener(v -> authViewModel.logout());

        // --- Bottom navigation ---
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

    @Nullable
    private static Fragment getFragment(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.page_map)       return new MapFragment();
        if (id == R.id.page_dashboard) return new HazardsFragment();
        if (id == R.id.page_account)   return new AccountFragment();
        if (id == R.id.page_settings)  return new SettingsFragment();
        return null;
    }
}
