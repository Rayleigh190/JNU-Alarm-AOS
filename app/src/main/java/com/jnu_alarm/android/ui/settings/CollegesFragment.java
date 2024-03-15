package com.jnu_alarm.android.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.jnu_alarm.android.R;

public class CollegesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.colleges_preferences, rootKey);
    }

}