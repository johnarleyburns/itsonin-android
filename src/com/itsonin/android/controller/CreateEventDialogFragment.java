package com.itsonin.android.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.itsonin.android.R;
import com.itsonin.android.api.ItsoninAPI;
import com.itsonin.android.entity.ApiError;
import com.itsonin.android.entity.Event;
import com.itsonin.android.enums.EventStatus;
import com.itsonin.android.enums.GuestStatus;
import com.itsonin.android.model.*;
import com.itsonin.android.resteasy.CustomDateTimeSerializer;
import com.squareup.timessquare.CalendarPickerView;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/9/14
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateEventDialogFragment extends DialogFragment {

    protected static final String TAG = CreateEventDialogFragment.class.toString();
    protected static final boolean DEBUG = true;
    protected static String mApiKey;

    protected static final String CATEGORY_INDEX_KEY = "categoryIndex";
    protected static final String EVENT_DATE_KEY = "eventDate";
    protected static final String START_TIME_KEY = "startTime";
    protected static final String END_TIME_KEY = "endTime";
    protected static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    protected static final String GEOCODE_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json";

    protected static final int DIALOG_LAYOUT_ID = R.layout.create_event_layout;

    protected LocalEvent localEvent;
    protected Host host;
    protected Place place;
    protected Category category;
    protected long eventId = -1;
    protected Date eventDate;
    protected Date startTime;
    protected Date endTime;
    protected double latitude;
    protected double longitude;

    protected ArrayAdapter<String> hostAutoAdapter;
    protected ArrayAdapter<String> placeAutoAdapter;
    protected PlacesAutoCompleteAdapter placesAutoCompleteAdapter;
    protected Location lastLocation;

    protected View overlay;
    protected ProgressBar progressBar;
    protected EditText titleView;
    protected EditText textView;
    protected AutoCompleteTextView hostView;
    protected AutoCompleteTextView placeView;
    protected TextView eventDateView;
    protected TextView startTimeView;
    protected TextView endTimeView;
    protected AutoCompleteTextView addressView;
    protected View detailsButton;
    protected View detailsExpand;
    protected View detailsCollapse;
    protected View detailsSection;
    protected TextView cancelButton;
    protected TextView saveButton;
    protected ItsoninAPI itsoninAPI;
    protected AlertDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            restoreBundleVariables(savedInstanceState);
        }

        if (DEBUG) {
            mApiKey = getResources().getString(R.string.debug_api_key);
        }
        else {
            mApiKey = getResources().getString(R.string.api_key);
        }

        final View view = createLayout(savedInstanceState);

        LocationManager locationManager = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider != null && !provider.isEmpty()) {
                locationManager.requestSingleUpdate(provider, locationListener, null);
            }
        }

        initDates(view.getContext());
        createDialog(view);
        if (localEvent != null) {
            initFromEvent(localEvent);
        }

        return dialog;
    }

    public void setLocalEvent(LocalEvent e) {
        localEvent = e;
    }

    protected void initFromEvent(LocalEvent e) {
        host.lastName = e.guestName;
        place.lastName = e.locationTitle;
        category.lastCategory = e.category;
        category.setIndex(category.getIndex());

        eventId = e._id;
        eventDate = new CustomDateTimeSerializer().parse(e.startTimeRaw);
        startTime = new CustomDateTimeSerializer().parse(e.startTimeRaw);
        endTime = new CustomDateTimeSerializer().parse(e.endTimeRaw);
        latitude = e.gpsLat;
        longitude = e.gpsLong;

        if (DEBUG) Log.e(TAG, "guestName: " + e.guestName);
        titleView.setText(e.title);
        textView.setText(e.description);
        hostView.setText(e.guestName);
        placeView.setText(e.locationTitle);
        eventDateView.setText(e.date);
        startTimeView.setText(e.startTime);
        endTimeView.setText(e.endTime);
        addressView.setText(e.locationAddress);
    }

    protected void createDialog(View view) {
        dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        setListeners();
    }

    protected void setListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocalEvent();
                if (DEBUG) Log.i(TAG, localEvent.toString());
                Context context = getActivity();
                if (context != null) {
                    persistToPrefs(context);
                }

                if (itsoninAPI == null) {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
                else {
                    overlay.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    itsoninAPI.createEvent(localEvent);
                }
            }
        });
    }

    protected BroadcastReceiver apiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            overlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            int statusCode = intent.getIntExtra(ItsoninAPI.ITSONIN_API_STATUS_CODE, 0);
            String path = intent.getStringExtra(ItsoninAPI.ITSONIN_API_PATH);
            String response = intent.getStringExtra(ItsoninAPI.ITSONIN_API_RESPONSE);
            if (DEBUG) Log.i(TAG, "received " + statusCode + ": " + response);

            ItsoninAPI.REST rest = ItsoninAPI.REST.valueOfPath(path);
            switch(rest) {
                case CREATE_EVENT:
                    if (isError(statusCode, response)) {
                        notifyAuthenticationError(context, response);
                    }
                    else {
                        Toast.makeText(context, R.string.created_event, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                    break;
                case UPDATE_EVENT:
                    if (isError(statusCode, response)) {
                        notifyAuthenticationError(context, response);
                    }
                    else {
                        dismiss();
                    }
                    break;
                default:
                    if (DEBUG) Log.i(TAG, "ignored rest api: " + rest);
                    dismiss();
                    break;
            }

        }

        protected boolean isError(int statusCode, String response) {
            return response == null || response.isEmpty() || statusCode != 200;
        }

        protected void notifyAuthenticationError(Context context) {
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
        }

        protected void notifyAuthenticationError(Context context, String response) {
            if (response == null || response.trim().length() == 0) {
                notifyAuthenticationError(context);
            }
            else {
                try {
                    ApiError apiError = ItsoninAPI.mapper.readValue(response, ApiError.class);
                    Toast.makeText(context, apiError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    Log.e(TAG, "error handling error", e);
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
            }
        }

    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        itsoninAPI = ItsoninAPI.instance(activity.getApplicationContext());
        itsoninAPI.unregisterReceiver(apiReceiver);
        itsoninAPI.registerReceiver(apiReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (itsoninAPI != null) {
            itsoninAPI.unregisterReceiver(apiReceiver);
            itsoninAPI = null;
        }
    }

    protected void restoreBundleVariables(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey(CATEGORY_INDEX_KEY)) {
                category.setIndex(bundle.getInt(CATEGORY_INDEX_KEY));
            }
            try {
                eventDate = DateFormat.getDateInstance().parse(bundle.getString(EVENT_DATE_KEY));
            }
            catch (ParseException e) {
                eventDate = Calendar.getInstance().getTime();
            }
            try {
                startTime = DateFormat.getDateInstance().parse(bundle.getString(START_TIME_KEY));
            }
            catch (ParseException e) {
                startTime = Calendar.getInstance().getTime();
            }
            try {
                endTime = DateFormat.getDateInstance().parse(bundle.getString(END_TIME_KEY));
            }
            catch (ParseException e) {
                endTime = Calendar.getInstance().getTime();
            }
        }
    }
    

    protected View inflateLayout() {
        return getActivity().getLayoutInflater().inflate(DIALOG_LAYOUT_ID, null);
    }

    protected View createLayout(Bundle savedInstanceState) {
        View view = inflateLayout();

        overlay = view.findViewById(R.id.overlay);
        progressBar = (ProgressBar)view.findViewById(R.id.progress);

        host = Host.load(view.getContext());
        place = Place.load(view.getContext());
        category = Category.load(view.getContext());

        titleView = (EditText)view.findViewById(R.id.title);
        textView = (EditText)view.findViewById(R.id.description);

        hostAutoAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, host.rememberedNames());
        hostView = (AutoCompleteTextView)view.findViewById(R.id.host);
        hostView.setAdapter(hostAutoAdapter);
        hostView.setOnItemClickListener(hostListener);
        if (host.lastName != null) {
            hostView.setText(host.lastName);
        }

        placeAutoAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, place.rememberedNames());
        placeView = (AutoCompleteTextView)view.findViewById(R.id.locationTitle);
        placeView.setAdapter(placeAutoAdapter);
        placeView.setOnItemClickListener(placeListener);
        //if (place.lastName != null) {
        //    placeView.setText(place.lastName);
        //}

        placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item);
        addressView = (AutoCompleteTextView)view.findViewById(R.id.locationAddress);
        addressView.setAdapter(placesAutoCompleteAdapter);
        addressView.setOnItemClickListener(placesAutoCompleteListener);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.event_categories_readable, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner)view.findViewById(R.id.category);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(eventCategoryListener);
        spinner.setSelection(category.getIndex());
        if (DEBUG) Log.i(TAG, "loaded category=" + category.lastCategory);

        eventDateView = (TextView)view.findViewById(R.id.date);
        eventDateView.setOnFocusChangeListener(eventDateFocusListener);
        eventDateView.setOnClickListener(eventDateClickListener);

        startTimeView = (TextView)view.findViewById(R.id.start_time);
        startTimeView.setOnFocusChangeListener(startTimeFocusListener);
        startTimeView.setOnClickListener(startTimeClickListener);

        endTimeView = (TextView)view.findViewById(R.id.end_time);
        endTimeView.setOnFocusChangeListener(endTimeFocusListener);
        endTimeView.setOnClickListener(endTimeClickListener);

        detailsSection = view.findViewById(R.id.detail_section);
        detailsExpand = view.findViewById(R.id.expand_details);
        detailsCollapse = view.findViewById(R.id.collapse_details);
        detailsButton = view.findViewById(R.id.detail_button);
        if (detailsButton != null) {
            detailsButton.setOnClickListener(detailsButtonListener);
        }

        cancelButton = (TextView)view.findViewById(R.id.event_dialog_action_cancel_button);
        saveButton = (TextView)view.findViewById(R.id.event_dialog_action_save_button);

        return view;
    }

    protected View.OnClickListener detailsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewGroup.LayoutParams params = detailsSection.getLayoutParams();
            if (params.height == 0) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                detailsExpand.setVisibility(View.GONE);
                detailsCollapse.setVisibility(View.VISIBLE);
            }
            else {
                params.height = 0;
                detailsExpand.setVisibility(View.VISIBLE);
                detailsCollapse.setVisibility(View.GONE);
            }
        }
    };

    protected AdapterView.OnItemClickListener hostListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = (String) parent.getItemAtPosition(position);
            hostView.setText(name);
        }
    };

    protected AdapterView.OnItemClickListener placeListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = (String) parent.getItemAtPosition(position);
            placeView.setText(name);
        }
    };

    protected Spinner.OnItemSelectedListener eventCategoryListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            category.setIndex(position);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    protected View.OnFocusChangeListener eventDateFocusListener = new View.OnFocusChangeListener() {
        protected static final String TAG = "eventDateFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                overlay.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                if (isAdded())
                    new EventDateDialog().show(getFragmentManager(), TAG);
            }
        }
    };
    
    protected View.OnClickListener eventDateClickListener = new View.OnClickListener() {
        protected static final String TAG = "eventDateClickListener";
        @Override
        public void onClick(View v) {
            overlay.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            new EventDateDialog().show(getFragmentManager(), TAG);
        }
    };

    protected View.OnFocusChangeListener startTimeFocusListener = new View.OnFocusChangeListener() {
        protected static final String TAG = "startTimeFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                new StartTimeDialog().show(getFragmentManager(), TAG);
            }
        }
    };

    protected View.OnClickListener startTimeClickListener = new View.OnClickListener() {
        protected static final String TAG = "startTimeFocusListener";
        @Override
        public void onClick(View v) {
            new StartTimeDialog().show(getFragmentManager(), TAG);
        }
    };

    protected View.OnFocusChangeListener endTimeFocusListener = new View.OnFocusChangeListener() {
        protected static final String TAG = "endTimeFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                new EndTimeDialog().show(getFragmentManager(), TAG);
            }
        }
    };

    protected View.OnClickListener endTimeClickListener = new View.OnClickListener() {
            protected static final String TAG = "endTimeFocusListener";
            @Override
            public void onClick(View v) {
                new EndTimeDialog().show(getFragmentManager(), TAG);
            }
    };
    
    protected class EventDateDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.calendar_dialog, null);

            Dialog d = new AlertDialog.Builder(getActivity())
                    //.setTitle(R.string.date_hint)
                    .setView(view)
                    .create();

            Calendar nextYear = Calendar.getInstance();
            nextYear.add(Calendar.YEAR, 1);

            CalendarPickerView calendarView = (CalendarPickerView) view.findViewById(R.id.calendar_view);
            Date today = new Date();
            calendarView.init(today, nextYear.getTime())
                    .withSelectedDate(eventDate);
            calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                @Override
                public void onDateSelected(Date date) {
                    eventDate = date;
                    eventDateView.setText(LocalEvent.friendlyDate(getActivity(), date));
                    dismissAllowingStateLoss();
                }
                @Override
                public void onDateUnselected(Date date) {
                }
            });

            overlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return d;
        }
    }

    protected void initDates(Context context) {
        initEventDate(context);
        initStartTime(context);
        initEndTime(context);
    }

    protected static final int LAST_HOUR_OF_DAY = 23;

    protected void initEventDate(Context context) {
        if (eventDate == null) {
            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            if (c.get(Calendar.HOUR_OF_DAY) >= LAST_HOUR_OF_DAY) {
                c.roll(Calendar.DAY_OF_YEAR, 1);
            }
            eventDate = c.getTime();
        }
        eventDateView.setText(LocalEvent.friendlyDate(context, eventDate));
    }

    protected void initStartTime(Context context) {
        Calendar c = Calendar.getInstance();
        c.roll(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        startTime = c.getTime();
        startTimeView.setText(LocalEvent.friendlyTime(context, startTime));
    }

    protected void initEndTime(Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.roll(Calendar.HOUR_OF_DAY, 1);
        endTime = c.getTime();
        endTimeView.setText(LocalEvent.friendlyTime(context, endTime));
    }

    protected class StartTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            c.setTime(startTime);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            startTime = c.getTime();
            //DateFormat df = DateFormat.getTimeInstance();
            DateFormat df = android.text.format.DateFormat.getTimeFormat(view.getContext());
            String s = df.format(startTime);
            startTimeView.setText(s);
            if (c.before(endTime)) {
                initEndTime(view.getContext());
            }
            dismissAllowingStateLoss();
        }
    }

    protected class EndTimeDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            if (endTime == null) {
                c.roll(Calendar.HOUR, 2);
                endTime = c.getTime();
            }
            else {
                c.setTime(endTime);
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, hourOfDay);
            now.set(Calendar.MINUTE, minute);
            endTime = now.getTime();
            //DateFormat df = DateFormat.getTimeInstance();
            DateFormat df = android.text.format.DateFormat.getTimeFormat(view.getContext());
            String s = df.format(endTime);
            endTimeView.setText(s);
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (DEBUG) Log.i(TAG, "onSaveInstanceState");
        bundle.putInt(CATEGORY_INDEX_KEY, category.getIndex());
        bundle.putString(EVENT_DATE_KEY, eventDate.toString());
        bundle.putString(START_TIME_KEY, startTime.toString());
        bundle.putString(END_TIME_KEY, endTime.toString());
    }

    protected void persistToPrefs(Context context) {
        String name = hostView.getText().toString().trim();
        host.lastName = name;
        if (!host.names.contains(name)) {
            host.names.add(name);
        }
        host.store(context);

        name = placeView.getText().toString().trim();
        place.lastName = name;
        if (!place.names.contains(name)) {
            place.names.add(name);
        }
        place.store(context);

        category.store(context);
    }

    protected ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;
        String jsonResults = null;
        try {
            String placesParams =  "input=" + URLEncoder.encode(input, ItsoninAPI.UTF8);
            if (lastLocation != null) {
                placesParams += "&location=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
            }
            placesParams += "&types=establishment";
            //String countryCode = getResources().getConfiguration().locale.getCountry();
            //sb.append("&components=country:" + countryCode);

            jsonResults = placesJSON(PLACES_API_BASE, placesParams);
            if (jsonResults == null) {
                if (DEBUG) Log.i(TAG, "no autocomplete found for input=" + input);
            }

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults);
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                //String id = predsJsonArray.getJSONObject(i).getString("id");
                String s = predsJsonArray.getJSONObject(i).getString("description");
                resultList.add(s);
                if (DEBUG) Log.i(TAG, "found result:" + s);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Cannot encode", e);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    protected class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        protected ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }

    protected AdapterView.OnItemClickListener placesAutoCompleteListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String str = (String) parent.getItemAtPosition(position);
            if (addressView != null)
                addressView.setText(str);
            updateLocationToAddress();
        }
    };

    protected void updateLocationToAddress() {
        String jsonResults = null;
        String input = addressView.getText().toString();
        try {
            jsonResults = placesJSON(GEOCODE_API_BASE,  "address=" + URLEncoder.encode(input, ItsoninAPI.UTF8));
            if (jsonResults == null) {
                if (DEBUG) Log.i(TAG, "no places found for input=" + input);
            }

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults);
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            for (int i = 0; i < predsJsonArray.length(); i++) {
                JSONObject addr = predsJsonArray.getJSONObject(i);
                JSONObject geometry = addr.getJSONObject("geometry");
                if (geometry == null) {
                    continue;
                }
                JSONObject location = geometry.getJSONObject("location");
                if (location == null) {
                    continue;
                }
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                latitude = lat;
                longitude = lng;
                if (DEBUG) Log.i(TAG, "found result:" + geometry);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Cannot encode", e);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }
    }

    // Define a listener that responds to location updates
    protected LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            lastLocation = location;
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };

    protected String placesJSON(String apiBase, String inputParam) {
        String jsonResults = null;

        if (getActivity() == null) {
            return null;
        }
        if (!itsoninAPI.isNetworkAvailable()) {
            if (DEBUG) Log.i(TAG, "No network available for autocomplete");
            return jsonResults;
        }

        try {
            String languageCode = getResources().getConfiguration().locale.getLanguage();
            StringBuilder sb = new StringBuilder(apiBase);
            sb.append("?sensor=true");
            sb.append("&key=" + mApiKey);
            sb.append("&language=" + languageCode);
            sb.append("&" + inputParam);

            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpGet myConnection = new HttpGet(sb.toString());
            if (DEBUG) Log.i(TAG, "calling url: " + sb.toString());
            response = myClient.execute(myConnection);
            jsonResults = EntityUtils.toString(response.getEntity(), ItsoninAPI.UTF8);
            if (DEBUG) Log.i(TAG, "results: " + jsonResults);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
        }

        return jsonResults;
    }

    protected void updateLocalEvent() {
        if (localEvent == null) {
            localEvent = new LocalEvent();
            localEvent._id = localEvent.hashCode();
            localEvent.numAttendees = 1; // myself
        }
        localEvent.status = EventStatus.ACTIVE.toString();
        localEvent.title = titleView.getText().toString();
        localEvent.description = textView.getText().toString();
        localEvent.guestName = hostView.getText().toString();
        localEvent.category = category.lastCategory;
        localEvent.date = new SimpleDateFormat(CustomDateTimeSerializer.ITSONIN_DATES).format(eventDate);
        localEvent.startTime = startTimeView.getText().toString();
        localEvent.endTime = endTimeView.getText().toString();
        localEvent.privateEvent = true; // can only create protected events via interface
        localEvent.locationTitle = placeView.getText().toString();
        localEvent.locationAddress = addressView.getText().toString();
        localEvent.gpsLat = latitude;
        localEvent.gpsLong = longitude;
    }
    
}
