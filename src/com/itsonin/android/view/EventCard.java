package com.itsonin.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import com.itsonin.android.R;
import com.itsonin.android.model.Event;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import org.apache.http.Header;
import org.apache.http.util.EntityUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class EventCard {

    public static final int list_item_layout = R.layout.event_card_item;

    public static final int[] VIEW_IDS = {
            R.id.event_card_main,
            R.id.event_card_title,
            R.id.event_card_text,
            R.id.event_card_host,
            R.id.event_card_icon,
            R.id.event_card_date,
            R.id.event_card_start_time,
            R.id.event_card_end_time,
            R.id.event_card_place,
            R.id.event_card_address,
            R.id.event_card_map,
            R.id.event_card_streetview,
            0
    };
  /*
            public static final String EVENT_ID = "_id";
        public static final String TITLE = "title";
        public static final String TEXT = "text";
        public static final String HOST = "host"; // name of person hosting the event
        public static final String CATEGORY = "category";
        public static final String DATE = "date";
        public static final String START_TIME = "startTime";
        public static final String END_TIME = "endTime";
        public static final String PLACE = "place";
        public static final String ADDRESS = "address";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String NUM_ATTENDEES = "numAttendees";
     */

    public static class EventViewBinder implements SimpleCursorAdapter.ViewBinder {

        public static final String TAG = EventViewBinder.class.getSimpleName();
        public static final boolean DEBUG = true;

        private Map<String, Drawable> eventIconMap = new HashMap<String, Drawable>();

        private void ensureEventIconMap(Context context) {
            if (eventIconMap.isEmpty()) {
                synchronized (eventIconMap) {
                    if (eventIconMap.isEmpty()) {
                        String[] eventCategories = context.getResources().getStringArray(R.array.event_categories);
                        TypedArray eventIcons = context.getResources().obtainTypedArray(R.array.event_icons);
                        for (int i = 0; i < eventCategories.length; i++) {
                            eventIconMap.put(eventCategories[i], eventIcons.getDrawable(i));
                        }
                    }
                }
            }
        }


        /**
             * Binds the Cursor column defined by the specified index to the specified view.
             *
             * When binding is handled by this ViewBinder, this method must return true.
             * If this method returns false, SimpleCursorAdapter will attempts to handle
             * the binding on its own.
             *
             * @param view the view to bind the data to
             * @param cursor the cursor to get the data from
             * @param columnIndex the column at which the data can be found in the cursor
             *
             * @return true if the data was bound to the view, false otherwise
             */

        private static final String STREET_VIEW_CHECK_URL_FORMAT =
                "https://maps.google.com/cbk" +
                        "?output=json" +
                        "&hl=en" +
                        "&ll=%1$f,%2$f" +
                        "&radius=50" +
                        "&cb_client=maps_sv" +
                        "&v=4";
        // %1$f = lat, %2$f = long

        private static final String STREET_VIEW_IMAGE_URL_FORMAT =
                "https://maps.googleapis.com/maps/api/streetview" +
                        "?size=%1$dx%2$d" +
                        "&location=%3$f,%4$f" +
                        "&sensor=false";
        // %1$d = image width, %2$d = image_height, %3$f = lat, %4$f = long

        private static final String MAPS_IMAGE_URL_FORMAT =
                "https://maps.googleapis.com/maps/api/staticmap" +
                        "?center=%3$f,%4$f" +
                        "&zoom=14" +
                        "&markers=color:0xf27935|%3$f,%4$f" +
                        "&size=%1$dx%2$d" +
                        "&scale=2" +
                        "&sensor=false";
        // %1$d = image width, %2$d = image_height, %3$f = lat, %4$f = long

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()) {
                case R.id.event_card_main:
                    return setEventCardMain(view, cursor);
                case R.id.event_card_map:
                    return setEventCardMap((ImageView) view, cursor);
                case R.id.event_card_streetview:
                    return setEventCardStreetView((ImageView) view, cursor);
                case R.id.event_card_icon:
                    return setEventCardIcon((ImageView) view, cursor.getString(columnIndex));
                case R.id.event_card_host:
                    return setEventCardCollapsableText((TextView)view, cursor.getString(columnIndex), R.string.hosted_by);
                case R.id.event_card_end_time:
                    return setEventCardCollapsableText((TextView)view, cursor.getString(columnIndex), R.string.until_time);
                case R.id.event_card_text:
                case R.id.event_card_place:
                case R.id.event_card_address:
                    return setEventCardCollapsableText((TextView)view, cursor.getString(columnIndex), 0);
                default:
                    return false;
            }
        }

        private boolean setEventCardMain(View view, Cursor cursor) {
            View directions = view.findViewById(R.id.event_card_directions);
            String address = cursor.getString(cursor.getColumnIndex(Event.Events.ADDRESS));
            if (address == null || address.trim().isEmpty()) {
                directions.setVisibility(View.GONE);
            }
            else {
                directions.setVisibility(View.VISIBLE);
            }
            return true;
        }

        private boolean setEventCardMap(ImageView view, Cursor cursor) {
            double lat = cursor.getDouble(cursor.getColumnIndex(Event.Events.LATITUDE));
            double lng = cursor.getDouble(cursor.getColumnIndex(Event.Events.LONGITUDE));
            if (lat == 0 && lng == 0) {
                view.setVisibility(View.GONE);
            }
            else {
                int displayWidth = view.getResources().getDisplayMetrics().widthPixels;
                int padding = view.getResources().getDimensionPixelSize(R.dimen.card_spacing);
                int width = displayWidth - padding - padding;
                int height = width / 2;
                String url = String.format(MAPS_IMAGE_URL_FORMAT, width/2, height/2, lat, lng);
                view.getLayoutParams().width = width;
                view.getLayoutParams().height = height;
                view.setBackgroundColor(R.color.card_image_placeholder);
                view.setVisibility(View.VISIBLE);
                if (DEBUG) Log.i(TAG, "setting map url=" + url);
                Picasso.with(view.getContext()).load(url).into(view);
            }
            return true;
        }

        private boolean setEventCardStreetView(ImageView view, Cursor cursor) {
            double lat = cursor.getDouble(cursor.getColumnIndex(Event.Events.LATITUDE));
            double lng = cursor.getDouble(cursor.getColumnIndex(Event.Events.LONGITUDE));
            if (lat == 0 && lng == 0) {
                view.setVisibility(View.GONE);
            }
            else {
                int displayWidth = view.getResources().getDisplayMetrics().widthPixels;
                int padding = view.getResources().getDimensionPixelSize(R.dimen.card_spacing);
                int width = displayWidth - padding - padding;
                int height = width / 2;
                view.setImageDrawable(null);
                asyncSetStreetView(new WeakReference<ImageView>(view), width, height, lat, lng);
            }
            return true;
        }

        private boolean setEventCardIcon(ImageView view, String category) {
            Context context = view.getContext();
            ensureEventIconMap(context);
            view.setImageDrawable(eventIconMap.get(category));
            return true;
        }

        private boolean setEventCardCollapsableText(TextView view, String text, int prefixStringId) {
            String prefix = prefixStringId > 0 ? view.getResources().getString(prefixStringId) : "";
            if (text == null || text.trim().isEmpty()) {
                view.setText("");
                view.setVisibility(View.GONE);
            }
            else {
                String displayText = prefix + text;
                view.setText(displayText);
                view.setVisibility(View.VISIBLE);
            }
            return true;
        }

        private void asyncSetStreetView(final WeakReference<ImageView> view,
                                       final int width, final int height,
                                       final double lat, final double lng) {
            AsyncHttpClient client = new AsyncHttpClient();
            final String url = String.format(STREET_VIEW_CHECK_URL_FORMAT, lat, lng);
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    boolean streetViewFound = false;
                    try {
                        String response = new String(responseBody, Xml.Encoding.UTF_8.name());
                        ImageView iv = view.get();
                        if (iv == null)
                            return;
                        if (response != null && response.trim().length() > 2) {
                            String url = String.format(STREET_VIEW_IMAGE_URL_FORMAT, width, height, lat, lng);
                            Picasso.with(iv.getContext()).load(url).into(iv);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "failed reading street view response for url=" + url);
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, "failed street view url=" + url);
                }
            });

        }

    }

}
