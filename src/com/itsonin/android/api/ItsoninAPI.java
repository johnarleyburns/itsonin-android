package com.itsonin.android.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.itsonin.android.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/20/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ItsoninAPI {

    private static final String TAG = ItsoninAPI.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String ITSONIN_API_ACTION = "com.itsonin.android.ITSONIN_API_ACTION";
    public static final String ITSONIN_API_PATH = "apiPath";
    public static final String ITSONIN_API_STATUS_CODE = "statusCode";
    public static final String ITSONIN_API_RESPONSE = "response";
    public static final String UTF8 = "UTF-8";

    public static enum REST {
        AUTHENTICATE("/api/device/create");
        private static final String BASE_URL = "http://itsonin-com.appspot.com";
        public String path;
        REST(String path) {
            this.path = path;
        }
        public String apiUrl() {
            return BASE_URL + path;
        }
        public static REST valueOfPath(String path) {
            for (REST r : REST.values()) {
                if (r.path.equals(path)) {
                    return r;
                }
            }
            return null;
        }
    }

    private WeakReference<Context> context;

    public ItsoninAPI() {
    }

    public ItsoninAPI(Context context) {
        this.context = new WeakReference<Context>(context);
    }

    public void authenticate() {
        if (context.get() == null) {
            Log.e(TAG, "null context reference");
            return;
        }
        try {
            JSONObject request = new JSONObject();
            request.put("type", "APPLICATION");
            String requestJSON = request.toString();
            asyncApiJSON(REST.AUTHENTICATE.apiUrl(), requestJSON);
        }
        catch (JSONException e) {
            Log.e(TAG, "Exception in authenticate()", e);
        }
    }

    private void asyncApiJSON(final String apiUrl, final String requestJSON) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiJSON(apiUrl, requestJSON);
            }
        }).start();
    }

    private void apiJSON(String apiUrl, String requestJSON) {
        if (!isNetworkAvailable()) {
            if (DEBUG) Log.i(TAG, "No network available for autocomplete");
            if (context.get() != null) {
                broadcastResult(REST.AUTHENTICATE.path, 408, context.get().getString(R.string.no_network));
            }
        }

        try {
            String locale = context.get().getResources().getConfiguration().locale.toString();
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpPost myConnection = new HttpPost(apiUrl);
            myConnection.setHeader("Accept", "application/json");
            myConnection.setHeader("Accept-Language", locale);
            myConnection.setHeader("Content-Type", "application/json");
            myConnection.setEntity(new StringEntity(requestJSON, UTF8));
            if (DEBUG) Log.i(TAG, "calling url: " + apiUrl + " body: " + requestJSON);
            response = myClient.execute(myConnection);
            String jsonResults = EntityUtils.toString(response.getEntity(), UTF8);
            if (DEBUG) Log.i(TAG, "results: " + jsonResults);
            broadcastResult(REST.AUTHENTICATE.path, response.getStatusLine().getStatusCode(), jsonResults);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing url", e);
            broadcastResult(REST.AUTHENTICATE.path, 400, context.get().getString(R.string.connection_error));
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to url", e);
            broadcastResult(REST.AUTHENTICATE.path, 408, context.get().getString(R.string.connection_error));
        }
    }

    private void broadcastResult(String path, int statusCode, String response) {
        Intent intent = new Intent(ITSONIN_API_ACTION);
        intent.putExtra(ITSONIN_API_PATH, path);
        intent.putExtra(ITSONIN_API_STATUS_CODE, statusCode);
        intent.putExtra(ITSONIN_API_RESPONSE, response);
        LocalBroadcastManager.getInstance(context.get()).sendBroadcast(intent);
    }

    public void registerReceiver(BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter(ITSONIN_API_ACTION);
        if (context.get() != null) {
            LocalBroadcastManager
                    .getInstance(context.get())
                    .registerReceiver(receiver, intentFilter);
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (context.get() != null) {
            LocalBroadcastManager
                    .getInstance(context.get())
                    .unregisterReceiver(receiver);
        }
    }

    public boolean isNetworkAvailable() {
        if (context.get() != null) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.get().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        else {
            return false;
        }
    }

}
