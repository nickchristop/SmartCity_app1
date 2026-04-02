package com.smartcity.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.smartcity.app.R;

import java.util.Locale;

/**
 * Settings fragment.
 * - Theme: Light / Dark
 * - Language: English / Greek (triggers Activity recreation)
 * - GPS Mock Location: user types lat/lng saved to SharedPrefs; used by ReportBottomSheet as override
 * - City Contact: static phone number (mock)
 */
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME    = "SmartCityPrefs";
    public static final String THEME_KEY     = "ThemeMode";
    public static final String LANG_KEY      = "Language";     // "en" or "el"
    public static final String MOCK_LAT_KEY  = "MockLat";
    public static final String MOCK_LNG_KEY  = "MockLng";
    // Default fallback: 2RR8+HM Marousi
    public static final double DEFAULT_LAT   = 38.04169;
    public static final double DEFAULT_LNG   = 23.80496;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        RadioGroup rgTheme    = view.findViewById(R.id.rg_theme);
        RadioGroup rgLanguage = view.findViewById(R.id.rg_language);

        // --- Theme ---
        int currentTheme = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO);
        rgTheme.check(currentTheme == AppCompatDelegate.MODE_NIGHT_YES
                ? R.id.rb_theme_dark : R.id.rb_theme_light);
        rgTheme.setOnCheckedChangeListener((g, id) -> {
            int mode = (id == R.id.rb_theme_dark)
                    ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            prefs.edit().putInt(THEME_KEY, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        // --- Language ---
        String savedLang = prefs.getString(LANG_KEY, "en");
        rgLanguage.check("el".equals(savedLang) ? R.id.rb_lang_el : R.id.rb_lang_en);
        rgLanguage.setOnCheckedChangeListener((g, id) -> {
            String langCode = (id == R.id.rb_lang_el) ? "el" : "en";
            if (!langCode.equals(prefs.getString(LANG_KEY, "en"))) {
                prefs.edit().putString(LANG_KEY, langCode).apply();
                requireActivity().recreate();
            }
        });

        // --- GPS Mock Location ---
        TextInputEditText etLat = view.findViewById(R.id.et_mock_lat);
        TextInputEditText etLng = view.findViewById(R.id.et_mock_lng);

        // Pre-fill with saved mock or default Marousi coords
        String savedLat = prefs.getString(MOCK_LAT_KEY, String.valueOf(DEFAULT_LAT));
        String savedLng = prefs.getString(MOCK_LNG_KEY, String.valueOf(DEFAULT_LNG));
        etLat.setText(savedLat);
        etLng.setText(savedLng);

        view.findViewById(R.id.btn_apply_mock).setOnClickListener(v -> {
            String latStr = etLat.getText() != null ? etLat.getText().toString().trim() : "";
            String lngStr = etLng.getText() != null ? etLng.getText().toString().trim() : "";
            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                prefs.edit()
                        .putString(MOCK_LAT_KEY, String.valueOf(lat))
                        .putString(MOCK_LNG_KEY, String.valueOf(lng))
                        .apply();
                Toast.makeText(getContext(), getString(R.string.msg_mock_applied), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.msg_mock_invalid), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Applied in MainActivity.attachBaseContext() so locale is respected on every launch.
     */
    public static Context applyLocale(Context base) {
        SharedPreferences prefs = base.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(LANG_KEY, "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        android.content.res.Configuration config =
                new android.content.res.Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }

    /** Read the user's mock/fallback location from SharedPrefs */
    public static double[] getMockLocation(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        double lat = Double.parseDouble(prefs.getString(MOCK_LAT_KEY, String.valueOf(DEFAULT_LAT)));
        double lng = Double.parseDouble(prefs.getString(MOCK_LNG_KEY, String.valueOf(DEFAULT_LNG)));
        return new double[]{lat, lng};
    }
}
