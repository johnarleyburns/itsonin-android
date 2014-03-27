package com.itsonin.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.itsonin.android.api.ItsoninAPI;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

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

    private static final String PREF_DEVICE_ID = "deviceId";
    private static final String PREF_DEVICE_TOKEN = "deviceToken";

    public String id;
    public String token;

    public Device() {
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_DEVICE_ID, id)
                .putString(PREF_DEVICE_TOKEN, token)
                .apply();
    }

    public static final Device load(Context context) {
        Device device = new Device();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        device.id = prefs.getString(PREF_DEVICE_ID, null);
        device.token = prefs.getString(PREF_DEVICE_TOKEN, null);
        if (DEBUG) Log.i(TAG, "read stored id=" + device.id + " token=" + device.token);
        return device;
    }

    public boolean exists() {
        return id != null && !id.trim().isEmpty()
                && token != null && !token.trim().isEmpty();
    }

}
