package com.itsonin.android.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.itsonin.android.R;
import com.itsonin.android.model.Category;
import com.itsonin.android.model.Event;
import com.itsonin.android.model.Host;
import com.itsonin.android.model.Place;
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

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/9/14
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddEventDialogFragment extends DialogFragment {

    private static final String TAG = AddEventDialogFragment.class.toString();
    private static final boolean DEBUG = true;
    private static String mApiKey;

    private static final String CATEGORY_INDEX_KEY = "categoryIndex";
    private static final String EVENT_DATE_KEY = "eventDate";
    private static final String START_TIME_KEY = "startTime";
    private static final String END_TIME_KEY = "endTime";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete";
    private static final String GEOCODE_API_BASE = "https://maps.googleapis.com/maps/api/geocode";
    private static final String OUT_JSON = "/json";
    private static final String UTF8 = "UTF-8";
    private static final String DATE_FORMAT_DAY_OF_WEEK = "EEEE";

    private Host host;
    private Place place;
    private Category category;
    private Date eventDate;
    private Date startTime;
    private Date endTime;
    private double latitude;
    private double longitude;

    private ArrayAdapter<String> hostAutoAdapter;
    private ArrayAdapter<String> placeAutoAdapter;
    private PlacesAutoCompleteAdapter placesAutoCompleteAdapter;
    private Location lastLocation;

    private View overlay;
    private ProgressBar progressBar;
    private EditText titleView;
    private EditText textView;
    private AutoCompleteTextView hostView;
    private AutoCompleteTextView placeView;
    private TextView eventDateView;
    private TextView startTimeView;
    private TextView endTimeView;
    private AutoCompleteTextView addressView;
    private View detailsButton;
    private View detailsExpand;
    private View detailsCollapse;
    private View detailsSection;

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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        initDates(view.getContext());

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_event)
                .setView(view)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .create();
    }

    private DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            Event e = extractEvent();
            if (DEBUG) Log.i(TAG, e.toString());
            Context context = getActivity();
            if (context != null) {
                Toast.makeText(getActivity(), "Created Event: " + e.title, Toast.LENGTH_SHORT).show();
                persistToPrefs(context);
            }
        }
    };

    private void restoreBundleVariables(Bundle bundle) {
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
    
    
    private View createLayout(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.add_event_layout, null);

        overlay = view.findViewById(R.id.overlay);
        progressBar = (ProgressBar)view.findViewById(R.id.progress);

        host = Host.load(view.getContext());
        place = Place.load(view.getContext());
        category = Category.load(view.getContext());

        titleView = (EditText)view.findViewById(R.id.title);
        textView = (EditText)view.findViewById(R.id.text);

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
        placeView = (AutoCompleteTextView)view.findViewById(R.id.place);
        placeView.setAdapter(placeAutoAdapter);
        placeView.setOnItemClickListener(placeListener);
        if (place.lastName != null) {
            placeView.setText(place.lastName);
        }

        placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item);
        addressView = (AutoCompleteTextView)view.findViewById(R.id.address);
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

        detailsButton = view.findViewById(R.id.detail_button);
        detailsSection = view.findViewById(R.id.detail_section);
        detailsExpand = view.findViewById(R.id.expand_details);
        detailsCollapse = view.findViewById(R.id.collapse_details);
        detailsButton.setOnClickListener(detailsButtonListener);

        return view;
    }

    private View.OnClickListener detailsButtonListener = new View.OnClickListener() {
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

    private AdapterView.OnItemClickListener hostListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = (String) parent.getItemAtPosition(position);
            hostView.setText(name);
        }
    };

    private AdapterView.OnItemClickListener placeListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = (String) parent.getItemAtPosition(position);
            placeView.setText(name);
        }
    };

    private Spinner.OnItemSelectedListener eventCategoryListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            category.setIndex(position);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private View.OnFocusChangeListener eventDateFocusListener = new View.OnFocusChangeListener() {
        private static final String TAG = "eventDateFocusListener";
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
    
    private View.OnClickListener eventDateClickListener = new View.OnClickListener() {
        private static final String TAG = "eventDateClickListener";
        @Override
        public void onClick(View v) {
            overlay.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            new EventDateDialog().show(getFragmentManager(), TAG);
        }
    };

    private View.OnFocusChangeListener startTimeFocusListener = new View.OnFocusChangeListener() {
        private static final String TAG = "startTimeFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                new StartTimeDialog().show(getFragmentManager(), TAG);
            }
        }
    };

    private View.OnClickListener startTimeClickListener = new View.OnClickListener() {
        private static final String TAG = "startTimeFocusListener";
        @Override
        public void onClick(View v) {
            new StartTimeDialog().show(getFragmentManager(), TAG);
        }
    };

    private View.OnFocusChangeListener endTimeFocusListener = new View.OnFocusChangeListener() {
        private static final String TAG = "endTimeFocusListener";
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                new EndTimeDialog().show(getFragmentManager(), TAG);
            }
        }
    };

    private View.OnClickListener endTimeClickListener = new View.OnClickListener() {
            private static final String TAG = "endTimeFocusListener";
            @Override
            public void onClick(View v) {
                new EndTimeDialog().show(getFragmentManager(), TAG);
            }
    };
    
    private class EventDateDialog extends DialogFragment {
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
                    eventDateView.setText(friendlyDate(date));
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

    private boolean isTomorrow(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        return DateUtils.isToday(c.getTime().getTime());
    }

    private boolean isCurrentWeek(Date date) {
        Calendar c = Calendar.getInstance();
        int currentWeek = c.get(Calendar.WEEK_OF_YEAR);
        c.setTime(date);
        int dateWeek = c.get(Calendar.WEEK_OF_YEAR);
        return currentWeek == dateWeek;
    }

    private String friendlyDate(Date date) { // no time component
        String s;
        if (DateUtils.isToday(date.getTime())) {
            s = getString(R.string.today);
        }
        else if (isTomorrow(date)) {
            s = getString(R.string.tomorrow);
        }
        else if (isCurrentWeek(date)) {
            s = new SimpleDateFormat(DATE_FORMAT_DAY_OF_WEEK).format(date);
        }
        else {
            s = DateFormat.getDateInstance().format(date);
        }
        return s;
    }

    private void initDates(Context context) {
        initEventDate(context);
        initStartTime(context);
        initEndTime(context);
    }

    private void initEventDate(Context context) {
        Date today = new Date();
        if (eventDate == null) {
            eventDate = today;
        }
        eventDateView.setText(friendlyDate(eventDate));
    }

    private void initStartTime(Context context) {
        Calendar c = Calendar.getInstance();
        c.roll(Calendar.HOUR, 1);
        c.set(Calendar.MINUTE, 0);
        startTime = c.getTime();
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        String s = df.format(startTime);
        startTimeView.setText(s);
    }

    private void initEndTime(Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.roll(Calendar.HOUR, 1);
        endTime = c.getTime();
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        String s = df.format(endTime);
        endTimeView.setText(s);
    }

    private class StartTimeDialog extends DialogFragment
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

    private class EndTimeDialog extends DialogFragment
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

    private void persistToPrefs(Context context) {
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

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;
        String jsonResults = null;
        try {
            String placesParams =  "input=" + URLEncoder.encode(input, UTF8);
            if (lastLocation != null) {
                placesParams += "&location=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
            }
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

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

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

    private AdapterView.OnItemClickListener placesAutoCompleteListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String str = (String) parent.getItemAtPosition(position);
            if (addressView != null)
                addressView.setText(str);
            updateLocationToAddress();
        }
    };

    private void updateLocationToAddress() {
        String jsonResults = null;
        String input = addressView.getText().toString();
        try {
            jsonResults = placesJSON(GEOCODE_API_BASE,  "address=" + URLEncoder.encode(input, UTF8));
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

    private boolean isNetworkAvailable() {
        Context context = getActivity() != null ? getActivity() : null;
        if (context != null) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        else {
            return false;
        }
    }

    // Define a listener that responds to location updates
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            lastLocation = location;
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };

    private String placesJSON(String apiBase, String inputParam) {
        String jsonResults = null;

        if (!isNetworkAvailable()) {
            if (DEBUG) Log.i(TAG, "No network available for autocomplete");
            return jsonResults;
        }

        try {
            String languageCode = getResources().getConfiguration().locale.getLanguage();
            StringBuilder sb = new StringBuilder(apiBase + OUT_JSON);
            sb.append("?sensor=true");
            sb.append("&key=" + mApiKey);
            sb.append("&language=" + languageCode);
            sb.append("&" + inputParam);

            HttpResponse response;
            HttpClient myClient = new DefaultHttpClient();
            HttpGet myConnection = new HttpGet(sb.toString());
            if (DEBUG) Log.i(TAG, "calling url: " + sb.toString());
            response = myClient.execute(myConnection);
            jsonResults = EntityUtils.toString(response.getEntity(), UTF8);
            if (DEBUG) Log.i(TAG, "results: " + jsonResults);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
        }

        return jsonResults;
    }

    private Event extractEvent() {
        Event e = new Event();
        e.title = titleView.getText().toString();
        e.text = textView.getText().toString();
        e.host = hostView.getText().toString();
        e.category = category.lastCategory;
        e.date = eventDate.toString();
        e.startTime = startTimeView.getText().toString();
        e.endTime = endTimeView.getText().toString();
        e.place = placeView.getText().toString();
        e.address = addressView.getText().toString();
        e.latitude = latitude;
        e.longitude = longitude;
        e.numAttendees = 1; // myself
        e._id = e.hashCode();
        return e;
    }

}
