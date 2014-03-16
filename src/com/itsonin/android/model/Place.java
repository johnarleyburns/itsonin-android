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
public class Place {

    private static final String PREF_PLACE_NAMES = "placeNames";

    public Set<String> names;

    public Place() {
        names = new HashSet<String>();
    }

    public Place(Set<String> names) {
        this.names = names;
    }

    public String[] rememberedNames() {
        String[] namesArray = new String[names.size()];
        return names.toArray(namesArray);
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREF_PLACE_NAMES, new HashSet<String>())
                .apply();
    }

    public static final Place load(Context context) {
        Place place = new Place();
        Set<String> names = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PREF_PLACE_NAMES, new HashSet<String>());
        place.names.addAll(names);
        return place;
    }

}
