package com.smartcity.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.smartcity.app.R;

import java.util.Locale;

/**
 * Settings fragment.
 * - Theme: Light / Dark (Follow System removed)
 * - Language: English / Greek (triggers Activity recreation so locale is applied immediately)
 */
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME = "SmartCityPrefs";
    public static final String THEME_KEY  = "ThemeMode";
    public static final String LANG_KEY   = "Language"; // "en" or "el"

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
        RadioButton rbEn      = view.findViewById(R.id.rb_lang_en);
        RadioButton rbEl      = view.findViewById(R.id.rb_lang_el);

        // --- Theme: restore saved selection (only Light / Dark) ---
        int currentTheme = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.rb_theme_dark);
        } else {
            rgTheme.check(R.id.rb_theme_light);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = (checkedId == R.id.rb_theme_dark)
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            prefs.edit().putInt(THEME_KEY, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        // --- Language: restore saved selection ---
        String savedLang = prefs.getString(LANG_KEY, "en");
        if ("el".equals(savedLang)) {
            rgLanguage.check(R.id.rb_lang_el);
        } else {
            rgLanguage.check(R.id.rb_lang_en);
        }

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String langCode = (checkedId == R.id.rb_lang_el) ? "el" : "en";
            String currentLang = prefs.getString(LANG_KEY, "en");

            // Only act if the selection actually changed
            if (!langCode.equals(currentLang)) {
                prefs.edit().putString(LANG_KEY, langCode).apply();
                // Recreate the Activity so the new locale is applied immediately
                requireActivity().recreate();
            }
        });

        return view;
    }

    /**
     * Applies the saved locale to the given Context.
     * Called from MainActivity.attachBaseContext() so every screen respects the locale.
     */
    public static android.content.Context applyLocale(android.content.Context base) {
        SharedPreferences prefs = base.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(LANG_KEY, "en");

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config =
                new android.content.res.Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }
}
