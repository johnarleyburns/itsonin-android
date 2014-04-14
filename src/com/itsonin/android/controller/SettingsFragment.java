package com.itsonin.android.controller;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;
import com.itsonin.android.R;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 4/14/14
 * Time: 7:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}