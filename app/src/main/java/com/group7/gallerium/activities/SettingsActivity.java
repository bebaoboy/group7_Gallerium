package com.group7.gallerium.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.group7.gallerium.R;
import com.group7.gallerium.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{


    Toolbar toolbar;
    public static final String KEY_PREF_UI = "uiKey";
    public static final String KEY_PREF_LOCATION = "locationKey";
    public static final String KEY_PREF_NUM_GRID = "numGridKey";
    public static final String KEY_PREF_LOCK_PRIVATE = "lockPrivateKey";
    public static final String KEY_PREF_LOCK_TRASH = "lockTrashKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        toolbar =  findViewById(R.id.setting_toolbar);
        toolbar.setTitle("Cài đặt");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setSubtitleTextAppearance(getApplicationContext(), R.style.ToolbarSubtitle);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        return false;
    }
}