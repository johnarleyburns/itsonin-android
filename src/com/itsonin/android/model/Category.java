package com.itsonin.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.itsonin.android.R;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/9/14
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Category {

    private static final String PREF_LAST_CATEGORY = "lastCategory";
    private static final int DEFAULT_CATEGORY_INDEX = 0;

    public String lastCategory;
    private String[] categories = {};

    public Category() {
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_CATEGORY, lastCategory)
                .apply();
    }

    public static final Category load(Context context) {
        Category category = new Category();
        category.categories = context.getResources().getStringArray(R.array.event_categories);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        category.lastCategory =  prefs.getString(PREF_LAST_CATEGORY, category.categories[DEFAULT_CATEGORY_INDEX]);
        return category;
    }

    public int getIndex() {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(lastCategory)) {
                return i;
            }
        }
        return DEFAULT_CATEGORY_INDEX;
    }

    public void setIndex(int i) {
        if (0 <= i && i < categories.length) {
            lastCategory = categories[i];
        }
        else {
            lastCategory = categories[DEFAULT_CATEGORY_INDEX];
        }
    }

}
