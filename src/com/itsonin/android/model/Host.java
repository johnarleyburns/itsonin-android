package com.itsonin.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/9/14
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Host {

    public Set<String> names = new HashSet<String>();
    public String lastName;

    public Host() {
    }

    public String[] rememberedNames() {
        String[] namesArray = new String[names.size()];
        return names.toArray(namesArray);
    }


    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(SavedPreference.PREF_HOST_NAMES, new HashSet<String>())
                .putString(SavedPreference.PREF_HOST_LAST_NAME, lastName)
                .apply();
    }

    public static final Host load(Context context) {
        Host host = new Host();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> names = prefs.getStringSet(SavedPreference.PREF_HOST_NAMES, new HashSet<String>());
        host.names.addAll(names);
        host.lastName =  prefs.getString(SavedPreference.PREF_HOST_LAST_NAME, null);
        return host;
    }

}
