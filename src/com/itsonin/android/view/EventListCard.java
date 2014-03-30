package com.itsonin.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.itsonin.android.R;
import com.itsonin.android.model.LocalEvent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import org.apache.http.Header;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class EventListCard {

    public static final int list_item_layout = R.layout.event_card_list_item;

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
            0,
            0,
            0,
            0
    };
  /*
            public static final String EVENT_ID = "_id";
        public static final String TITLE = "title";
        public static final String TEXT = "description";
        public static final String HOST = "host"; // name of person hosting the event
        public static final String CATEGORY = "category";
        public static final String DATE = "date";
        public static final String START_TIME = "startTime";
        public static final String END_TIME = "endTime";
        public static final String PLACE = "locationTitle";
        public static final String ADDRESS = "locationAddress";
        public static final String LATITUDE = "gpsLat";
        public static final String LONGITUDE = "gpsLong";
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

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()) {
                case R.id.event_card_main:
                    return true;
                case R.id.event_card_icon:
                    return setEventCardIcon((ImageView) view, cursor.getString(columnIndex));
                case R.id.event_card_host:
                    return setEventCardCollapsableText((TextView) view, cursor.getString(columnIndex), R.string.hosted_by);
                case R.id.event_card_end_time:
                    return setEventCardCollapsableText((TextView) view, cursor.getString(columnIndex), R.string.until_time);
                case R.id.event_card_text:
                case R.id.event_card_place:
                    return setEventCardCollapsableText((TextView)view, cursor.getString(columnIndex), 0);
                default:
                    return false;
            }
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

    }

}
