package com.jnu_alarm.android.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.jnu_alarm.android.BuildConfig;
import com.jnu_alarm.android.R;
import com.jnu_alarm.android.WebActivity;

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

        findPreference("contact").setOnPreferenceClickListener(preference -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_navigation_settings_to_navigation_contact);
            return true;
        });

        findPreference("PUSH").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("https://wackitlab.notion.site/2800db7021a24e59adcd4f27f1673be4");
            intent.setData(uri);
            startActivity(intent);
            return true;
        });

        findPreference("sponsor").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), WebActivity.class);
            intent.putExtra("link", "https://wackitlab.notion.site/1c5a516070804fa5a0dd30d43a486979");
            intent.putExtra("title", "후원하기");
            startActivity(intent);
            return true;
        });

        findPreference("FAQ").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), WebActivity.class);
            intent.putExtra("link", "https://wackitlab.notion.site/FAQ-b0f2438e25574315baa0962d1dd250e5");
            intent.putExtra("title", "FAQ");
            startActivity(intent);
            return true;
        });

        findPreference("info").setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), InfoActivity.class);
            startActivity(intent);
            return true;
        });

        // 디버그 모드에서만 개발자 모드 활성화
        if (BuildConfig.DEBUG) {
            Preference devModePreference = findPreference("dev");
            devModePreference.setEnabled(true);
            devModePreference.setVisible(true);
        }
    }
}