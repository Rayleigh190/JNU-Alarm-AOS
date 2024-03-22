package com.jnu_alarm.android.ui.settings;

import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.preference.PreferenceFragmentCompat;

import com.jnu_alarm.android.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // 액션을 실행하여 navigation_colleges로 이동하는 코드
        findPreference("college").setOnPreferenceClickListener(preference -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_settings_to_navigation_colleges);
            return true;
        });

        findPreference("department").setOnPreferenceClickListener(preference -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_settings_to_navigation_departments);
            return true;
        });

        findPreference("business").setOnPreferenceClickListener(preference -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_settings_to_navigation_business);
            return true;
        });
    }
}