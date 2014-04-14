package com.itsonin.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 4/14/14
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SavedPreference {

    public static final String PREF_DEVICE_ID = "deviceId";
    public static final String PREF_DEVICE_TOKEN = "deviceToken";
    public static final String PREF_HOST_NAMES = "hostNames";
    public static final String PREF_HOST_LAST_NAME = "hostLastName";
    public static final String PREF_LAST_CATEGORY = "lastCategory";
    public static final String PREF_PLACE_NAMES = "placeNames";
    public static final String PREF_PLACE_LAST_NAME = "placeLastName";

    public static final String[] PREFERENCES = {
            PREF_DEVICE_ID,
            PREF_DEVICE_TOKEN,
            PREF_HOST_NAMES,
            PREF_HOST_LAST_NAME,
            PREF_LAST_CATEGORY,
            PREF_PLACE_NAMES,
            PREF_PLACE_LAST_NAME
    };

    public static void clearAll(Context context) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();
        for (String pref : PREFERENCES) {
            editor.remove(pref);
        }
        editor.apply();
    }
}
