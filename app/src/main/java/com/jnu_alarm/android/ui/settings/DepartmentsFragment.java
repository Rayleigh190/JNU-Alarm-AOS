package com.jnu_alarm.android.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.jnu_alarm.android.R;

public class DepartmentsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.departments_preferences, rootKey);
    }

}