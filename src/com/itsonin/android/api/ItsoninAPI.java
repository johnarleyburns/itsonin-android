package com.itsonin.android.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsonin.android.R;
import com.itsonin.android.entity.Event;
import com.itsonin.android.entity.EventWithGuest;
import com.itsonin.android.model.Device;
import com.itsonin.android.model.LocalEvent;
import com.itsonin.android.model.Session;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
    public static final ObjectMapper mapper = new ObjectMapper();

    public static enum REST {
        CREATE_DEVICE("/api/device/create", HttpMethod.POST),
        AUTHENTICATE("/api/device/%1$s/createDevice/%2$s", HttpMethod.GET),
        CREATE_EVENT("/api/event/create", HttpMethod.POST),
        LIST_EVENTS("/api/event/list", HttpMethod.GET);

        private static final String BASE_URL = "http://itsonin-com.appspot.com";
        public String path;
        public String method;

        REST(String path, String method) {
            this.path = path;
            this.method = method;
        }

        public String apiUrl() {
            return BASE_URL + path;
        }

        public String apiUrl(String ... args) {
            return BASE_URL + String.format(path, (Object)args);
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

    private static final String JSESSIONID_HEADER = "JSESSIONID";
    private static final String SESSION_TOKEN_HEADER = "token";

    private WeakReference<Context> context;

    private Device device;
    private Session session;
    private LocalEvent pendingLocalEvent;
    private boolean pendingListEvents;

    public ItsoninAPI() {
    }

    public ItsoninAPI(Context context) {
        this.context = new WeakReference<Context>(context);
        device = Device.load(context);
        session = Session.load(context);
        registerReceiver(apiReceiver);
    }

    public void onDestroy() {
        unregisterReceiver(apiReceiver);
    }

    public void createDevice() {
        if (context.get() == null) {
            Log.e(TAG, "null context reference");
            return;
        }
        try {
            JSONObject request = new JSONObject();
            request.put("type", "APPLICATION");
            String requestJSON = request.toString();
            asyncApiJSON(REST.CREATE_DEVICE, REST.CREATE_DEVICE.apiUrl(), requestJSON);
        }
        catch (JSONException e) {
            Log.e(TAG, "Exception in createDevice()", e);
        }
    }

    public void authenticate() {
        if (context.get() == null) {
            Log.e(TAG, "null context reference");
            return;
        }
        if (device == null || !device.exists()) {
            createDevice();
        }
        else {
            asyncApiJSON(REST.AUTHENTICATE, REST.AUTHENTICATE.apiUrl(device.id, device.token), "");
        }
    }

    public void createEvent(LocalEvent localEvent) {
        pendingLocalEvent = localEvent;
        if (context.get() == null) {
            Log.e(TAG, "null context reference");
            return;
        }
        if (session == null || !session.exists()) {
            authenticate();
            return;
        }
        try {
            String requestJSON = mapper.writeValueAsString(localEvent.toEventWithGuest(context.get()));
            asyncApiJSON(REST.CREATE_EVENT, REST.CREATE_EVENT.apiUrl(), requestJSON);
        }
        catch (JsonProcessingException e) {
            Log.e(TAG, "Exception in createEvent()", e);
        }
    }

    public void listEvents() {
        pendingListEvents = true;
        if (context.get() == null) {
            Log.e(TAG, "null context reference");
            return;
        }
        if (session == null || !session.exists()) {
            authenticate();
            return;
        }
        asyncApiJSON(REST.LIST_EVENTS, REST.LIST_EVENTS.apiUrl(), "");
    }

    private void asyncApiJSON(final REST rest, final String apiUrl, final String requestJSON) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiJSON(rest, apiUrl, requestJSON);
            }
        }).start();
    }

    private void apiJSON(REST rest, String apiUrl, String requestJSON) {
        if (!isNetworkAvailable()) {
            if (DEBUG) Log.i(TAG, "No network available for autocomplete");
            if (context.get() != null) {
                broadcastResult(rest.path, 408, context.get().getString(R.string.no_network));
            }
        }

        try {
            String locale = context.get().getResources().getConfiguration().locale.toString();
            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();

            HttpUriRequest myConnection;
            if (HttpMethod.GET.equals(rest.method)) {
                myConnection = new HttpGet(apiUrl);
            }
            else if (HttpMethod.POST.equals(rest.method)) {
                HttpPost myPost = new HttpPost(apiUrl);
                myPost.setEntity(new StringEntity(requestJSON, UTF8));
                myConnection = myPost;
            }
            else {
                Log.e(TAG, "unsupported method:" + rest.method);
                return;
            }
            myConnection.setHeader("Accept", "application/json");
            myConnection.setHeader("Accept-Language", locale);
            myConnection.setHeader("Content-Type", "application/json");
            if (DEBUG) Log.i(TAG, "calling url: " + apiUrl + " body: " + requestJSON);

            response = myClient.execute(myConnection);
            handleCookies(response);
            if (response.getStatusLine().getStatusCode() == 401) {
                if (DEBUG) Log.i(TAG, "need to re-authenticate");
                authenticate();
                return;
            }

            String jsonResults = EntityUtils.toString(response.getEntity(), UTF8);
            if (DEBUG) Log.i(TAG, "results: " + jsonResults);
            broadcastResult(rest.path, response.getStatusLine().getStatusCode(), jsonResults);

        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing url", e);
            broadcastResult(rest.path, 400, context.get().getString(R.string.connection_error));
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to url", e);
            broadcastResult(rest.path, 408, context.get().getString(R.string.connection_error));
        }
    }

    private void handleCookies(HttpResponse response) {
        Header[] cookieHeaders = response.getHeaders("Set-Cookie");
        if (cookieHeaders == null || cookieHeaders.length == 0) {
            return;
        }

        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        for (Header cookieHeader : cookieHeaders) {
            List<HttpCookie> parsedCookies = HttpCookie.parse(cookieHeader.getValue());
            cookies.addAll(parsedCookies);
        }

        boolean matched = false;
        for (HttpCookie cookie : cookies) {
            if (DEBUG) Log.i(TAG, "cookie name=" + cookie.getName() + " value=" + cookie.getValue());
            if (JSESSIONID_HEADER.equals(cookie.getName())) {
                session.sessionId = cookie.getValue();
                matched = true;
            }
            else if (SESSION_TOKEN_HEADER.equals(cookie.getName())) {
                session.sessionToken = cookie.getValue();
                matched = true;
            }
        }

        if (matched && context.get() != null) {
            session.store(context.get());
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

    private BroadcastReceiver apiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int statusCode = intent.getIntExtra(ItsoninAPI.ITSONIN_API_STATUS_CODE, 0);
            String path = intent.getStringExtra(ItsoninAPI.ITSONIN_API_PATH);
            String response = intent.getStringExtra(ItsoninAPI.ITSONIN_API_RESPONSE);
            if (DEBUG) Log.i(TAG, "received " + statusCode + ": " + response);

            if (response == null || response.isEmpty() || statusCode != 200) {
                Log.e(TAG, "Empty response statusCode=" + statusCode);
                notifyAuthenticationError(context);
                return;
            }

            ItsoninAPI.REST rest = ItsoninAPI.REST.valueOfPath(path);
            switch(rest) {
                case CREATE_DEVICE:
                    handleCreateDevice(context, response);
                    break;
                case AUTHENTICATE:
                    handleAuthenticate(context, response);
                    break;
                case CREATE_EVENT:
                    handleCreateEvent(context, response);
                    break;
                case LIST_EVENTS:
                    handleListEvents(context, response);
                    break;
                default:
                    if (DEBUG) Log.i(TAG, "ignored rest api: " + rest);
                    break;
            }
        }

        private void handleCreateDevice(Context context, String response) {
            try {
                JSONObject jsonObj = new JSONObject(response);
                String token = jsonObj.getString("token");
                if (DEBUG) Log.i(TAG, "received device token=" + token);
                if (token == null || token.trim().isEmpty()) {
                    Log.e(TAG, "Empty device token returned");
                    notifyAuthenticationError(context);
                    return;
                }
                device.token = token;
                device.store(context);
                Toast.makeText(context, "authenticated with new device creation", Toast.LENGTH_SHORT).show();
                handlePendingEvents();
            } catch (JSONException e) {
                Log.e(TAG, "Cannot process JSON results", e);
                notifyAuthenticationError(context);
            }
        }

        private void handleAuthenticate(Context context, String response) {
            if (DEBUG) Log.i(TAG, "received authenticate");
            handlePendingEvents();
        }

        private void handleCreateEvent(Context context, String response) {
            try {
                JSONObject jsonObj = new JSONObject(response);
                if (DEBUG) Log.i(TAG, "received create event response=" + jsonObj);
                if (pendingLocalEvent != null) {
                    pendingLocalEvent = null;
                }

            } catch (JSONException e) {
                Log.e(TAG, "Cannot process JSON results", e);
                notifyAuthenticationError(context);
            }
        }

        private void handleListEvents(Context context, String response) {
            if (DEBUG) Log.i(TAG, "received list events response");
            if (pendingListEvents) {
                pendingListEvents = false;
            }
        }

        private void notifyAuthenticationError(Context context) {
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
        }

        private void handlePendingEvents() {
            if (pendingLocalEvent != null) {
                createEvent(pendingLocalEvent);
            }
            if (pendingListEvents) {
                listEvents();
            }
        }

    };
}
