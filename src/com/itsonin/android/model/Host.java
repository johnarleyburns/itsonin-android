package com.itsonin.android.model;

import android.content.Context;
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

    private static final String PREF_HOST_NAMES = "hostNames";

    public Set<String> names;

    public Host() {
        names = new HashSet<String>();
    }

    public Host(Set<String> names) {
        this.names = names;
    }

    public String[] rememberedNames() {
        String[] namesArray = new String[names.size()];
        return names.toArray(namesArray);
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREF_HOST_NAMES, new HashSet<String>())
                .apply();
    }

    public static final Host load(Context context) {
        Host host = new Host();
        Set<String> names = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PREF_HOST_NAMES, new HashSet<String>());
        host.names.addAll(names);
        return host;
    }

}
