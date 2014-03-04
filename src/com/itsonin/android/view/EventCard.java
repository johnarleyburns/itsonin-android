package com.itsonin.android.view;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.itsonin.android.R;
import com.itsonin.android.model.Event;
import com.squareup.picasso.Picasso;

public class EventCard {

    public static final int list_item_layout = R.layout.event_card_item;

    public static final int[] VIEW_IDS = {
            0,
            R.id.event_card_title,
            R.id.event_card_text,
            R.id.event_card_host,
            0,
            R.id.event_card_date,
            R.id.event_card_start_time,
            R.id.event_card_end_time,
            R.id.event_card_place,
            R.id.event_card_address,
            R.id.event_card_image,
            0,
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

            private static final String STREET_VIEW_URL_FORMAT =
                    "https://maps.googleapis.com/maps/api/streetview?size=%1$dx%2$d&location=%3$f,%4$f&sensor=false";
                    // %1$d = image width, %2$d = image_height, %3$f = lat, %4$f = long

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.event_card_image)
                    return setEventCardImage((ImageView) view, cursor);
                else
                    return false;
            }

            private boolean setEventCardImage(ImageView view, Cursor cursor) {
                double lat = cursor.getDouble(cursor.getColumnIndex(Event.Events.LATITUDE));
                double lng = cursor.getDouble(cursor.getColumnIndex(Event.Events.LONGITUDE));
                int displayWidth = view.getResources().getDisplayMetrics().widthPixels;
                int padding = view.getResources().getDimensionPixelSize(R.dimen.card_spacing);
                int width = displayWidth - padding - padding;
                int height = width / 2;
                String url = String.format(STREET_VIEW_URL_FORMAT, width, height, lat, lng);
                //url = "https://upload.wikimedia.org/wikipedia/en/9/92/Pok%C3%A9mon_episode_1_screenshot.png";
                if (DEBUG) Log.i(TAG, "setEventCardImage() view=" + view + " url=" + url);
                view.getLayoutParams().width = width;
                view.getLayoutParams().height = height;
                view.setBackgroundColor(R.color.card_image_placeholder);
                Picasso.with(view.getContext()).load(url).into(view);
                return true;
            }

    }

}
