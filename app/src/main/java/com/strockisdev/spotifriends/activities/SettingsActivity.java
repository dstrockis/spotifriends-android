package com.strockisdev.spotifriends.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.strockisdev.spotifriends.R;

import java.util.List;


public class SettingsActivity extends Activity {

    public static final String KEY_NETWORK_PREF = "pref_isWifiOnly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_NETWORK_PREF)) {
                String value = sharedPreferences.getString(KEY_NETWORK_PREF, "True");
                Preference networkPref = findPreference(KEY_NETWORK_PREF);
                if (value.equals("False"))
                    networkPref.setSummary(R.string.mobile_data_pref_summary_false);
                else
                    networkPref.setSummary(R.string.mobile_data_pref_summary_true);

            }
        }
    }
}
