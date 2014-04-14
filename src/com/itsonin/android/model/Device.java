package com.itsonin.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/9/14
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Device {

    private static final String TAG = Device.class.getSimpleName();
    private static final boolean DEBUG = true;

    public String id;
    public String token;

    public Device() {
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SavedPreference.PREF_DEVICE_ID, id)
                .putString(SavedPreference.PREF_DEVICE_TOKEN, token)
                .apply();
    }

    public static final Device load(Context context) {
        Device device = new Device();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        device.id = prefs.getString(SavedPreference.PREF_DEVICE_ID, null);
        device.token = prefs.getString(SavedPreference.PREF_DEVICE_TOKEN, null);
        if (DEBUG) Log.i(TAG, "read stored id=" + device.id + " token=" + device.token);
        return device;
    }

    public boolean exists() {
        return id != null && !id.trim().isEmpty()
                && token != null && !token.trim().isEmpty();
    }

}
