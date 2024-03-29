package com.itsonin.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.itsonin.android.R;
import com.itsonin.android.enums.EventStatus;
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
            0,
            R.id.event_card_title,
            0,
            R.id.event_card_icon,
            R.id.event_card_pyramid_icon,
            0,
            0,
            0,
            R.id.event_card_date,
            R.id.event_card_start_time,
            R.id.event_card_end_time,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
    };

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
            if (DEBUG) Log.i(TAG, "setViewValue() row=" + cursor.getPosition() + " col=" + columnIndex + " val=" + cursor.getString(columnIndex));
            switch (view.getId()) {
                case R.id.event_card_main:
                    return setEventCardMain(view, cursor);
                case R.id.event_card_title:
                    //if (DEBUG) Log.i(TAG, "title str=" + cursor.getString(columnIndex));
                    return setEventCardCollapsableText((TextView)view, cursor.getString(columnIndex), 0);
                case R.id.event_card_icon:
                    //if (DEBUG) Log.i(TAG, "icon str=" + cursor.getString(columnIndex));
                    return setEventCardIcon((ImageView) view, cursor.getString(columnIndex));
                case R.id.event_card_pyramid_icon:
                    return setEventCardPyramidIcon((ImageView) view, cursor.getString(columnIndex));
                case R.id.event_card_end_time:
                    return setEventCardCollapsableText((TextView) view, cursor.getString(columnIndex), R.string.until_time);
                default:
                    return false;
            }
        }

        private boolean setEventCardMain(View view, Cursor cursor) {
            setTimeSeparator(view, cursor);
            setPlaceView(view, cursor);
            return true;
        }

        private boolean setTimeSeparator(View view, Cursor cursor) {
            View timeSeparator = view.findViewById(R.id.event_card_time_separator);
            String startTime = cursor.getString(cursor.getColumnIndex(LocalEvent.Events.START_TIME));
            boolean timeVisible = startTime != null && !startTime.trim().isEmpty();
            if (timeVisible) {
                timeSeparator.setVisibility(View.VISIBLE);
            }
            else {
                timeSeparator.setVisibility(View.GONE);
            }
            return true;
        }

        private boolean setPlaceView(View view, Cursor cursor) {
            TextView placeView = (TextView)view.findViewById(R.id.event_card_place);

            String locationTitle = cursor.getString(cursor.getColumnIndex(LocalEvent.Events.LOCATION_TITLE));
            String locationAddress = cursor.getString(cursor.getColumnIndex(LocalEvent.Events.LOCATION_ADDRESS));
            String status = cursor.getString(cursor.getColumnIndex(LocalEvent.Events.STATUS));
            if (DEBUG) Log.i(TAG, "eventStatus=" + status);
            EventStatus s = (status == null || status.isEmpty()) ? EventStatus.ACTIVE : EventStatus.valueOf(status);
            String text;
            switch (s) {
                default:
                case ACTIVE:
                    text = locationTitle != null && !locationTitle.trim().isEmpty() ? locationTitle : locationAddress;
                    placeView.setTextColor(view.getResources().getColor(R.color.itsonin_body_text));
                    return setEventCardCollapsableText(placeView, text, 0);
                case CANCELLED:
                    text = "<b>" + view.getContext().getString(R.string.event_cancelled) + "</b>";
                    placeView.setTextColor(view.getResources().getColor(R.color.itsonin_secondary_brand_action_button));
                    placeView.setText(Html.fromHtml(text));
                    return true;
                case EXPIRED:
                    text = "<b>" + view.getContext().getString(R.string.event_expired) + "</b>";
                    placeView.setTextColor(view.getResources().getColor(R.color.itsonin_secondary_brand_action_button));
                    placeView.setText(Html.fromHtml(text));
                    return true;
            }
        }

        private boolean setEventCardPyramidIcon(ImageView view, String sharability) {
            boolean isPyramid = Math.random() < 0.5;
            if (isPyramid) {
                view.setVisibility(View.VISIBLE);
            }
            else {
                view.setVisibility(View.GONE);
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

    }

}
