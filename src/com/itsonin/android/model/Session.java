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
public class Session {

    private static final String TAG = Session.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final String PREF_SESSION_ID = "sessionId";
    private static final String PREF_SESSION_TOKEN = "sessionToken";

    public String sessionId;
    public String sessionToken;

    public Session() {
    }

    public void store(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SESSION_ID, sessionId)
                .putString(PREF_SESSION_TOKEN, sessionToken)
                .apply();
    }

    public static final Session load(Context context) {
        Session session = new Session();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        session.sessionId = prefs.getString(PREF_SESSION_ID, null);
        session.sessionToken = prefs.getString(PREF_SESSION_TOKEN, null);
        if (DEBUG) Log.i(TAG, "read stored sessionId=" + session.sessionId + " sessionToken=" + session.sessionToken);
        return session;
    }

    public boolean exists() {
        return sessionId != null && !sessionId.trim().isEmpty()
                && sessionToken != null && !sessionToken.trim().isEmpty();
    }

}
