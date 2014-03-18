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
public class Place {

    private static final String PREF_PLACE_NAMES = "placeNames";
    private static final String PREF_PLACE_LAST_NAME = "placeLastName";

    public Set<String> names = new HashSet<String>();
    public String lastName;

    public Place() {
    }

    public String[] rememberedNames() {
        String[] namesArray = new String[names.size()];
        return names.toArray(namesArray);
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREF_PLACE_NAMES, new HashSet<String>())
                .putString(PREF_PLACE_LAST_NAME, lastName)
                .apply();
    }

    public static final Place load(Context context) {
        Place place = new Place();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> names = prefs.getStringSet(PREF_PLACE_NAMES, new HashSet<String>());
        place.names.addAll(names);
        place.lastName =  prefs.getString(PREF_PLACE_LAST_NAME, null);
        return place;
    }

}
