package com.itsonin.android.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.*;

import android.text.format.DateUtils;
import android.util.Log;
import com.itsonin.android.entity.Event;
import com.itsonin.android.enums.EventVisibility;
import com.itsonin.android.model.LocalEvent;
import com.itsonin.android.model.LocalEvent.Events;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/2/14
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventsContentProvider extends ContentProvider {

    private static final String TAG = EventsContentProvider.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String AUTHORITY = "com.itsonin.android.providers.EventsContentProvider";

    public static final String EVENTS_TABLE_NAME = "events";
    public static final String EVENTS_ID_PATH = EVENTS_TABLE_NAME + "/event/";
    public static final String EVENTS_ID_PATH_MATCH = EVENTS_ID_PATH + "*";
    public static final String EVENTS_PRIVATE_PATH = EVENTS_TABLE_NAME + "/private/";
    public static final String EVENTS_DISCOVER_PATH = EVENTS_TABLE_NAME + "/discover/";
    public static final String EVENTS_DISCOVER_PATH_MATCH = EVENTS_DISCOVER_PATH + "*";

    private static final int EVENTS = 1;
    private static final int EVENTS_ID = 2;
    private static final int EVENTS_PRIVATE = 3;
    private static final int EVENTS_DISCOVER = 4;

    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> eventsProjectionMap;

    private static List<Event> discoverEvents = Collections.synchronizedList(new ArrayList<Event>());
    private static Map<String, Event> cachedEvents = Collections.synchronizedMap(new HashMap<String, Event>());

    public static synchronized void setDiscoverEvents(List<Event> events) {
        discoverEvents.clear();
        discoverEvents.addAll(events);
    }

    public static void cacheEvent(Event event) {
        cachedEvents.put(event.getEventId().toString(), event);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EVENTS:
            case EVENTS_ID:
            case EVENTS_PRIVATE:
            case EVENTS_DISCOVER:
                return Events.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != EVENTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        return null;
        /*
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(NOTES_TABLE_NAME, Notes.TEXT, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
        */
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private boolean isEventCurrent(Event e, Date now) {
        if (e.getEndTime() != null && e.getEndTime().before(now)) {
            return false;
        }
        else if (e.getStartTime() == null) {
            return true;
        }
        else if (e.getStartTime().after(now)) {
            return true;
        }
        else if (DateUtils.isToday(e.getStartTime().getTime())) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isEventPrivate(Event e) {
        if (e.getVisibility() == EventVisibility.PUBLIC) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG) Log.i(TAG, "query() uri=" + uri);
        /*
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(NOTES_TABLE_NAME);
        qb.setProjectionMap(notesProjectionMap);
        */
        MatrixCursor c = new MatrixCursor(Events.COLUMNS, 1);
        switch (sUriMatcher.match(uri)) {
            case EVENTS:
                Date now = new Date();
                synchronized (discoverEvents) {
                    for (Event e : discoverEvents) {
                        if (isEventCurrent(e, now)) {
                            LocalEvent le = new LocalEvent(getContext(), e);
                            c.addRow(le.makeCursorRow());
                        }
                    }
                }
                break;
            case EVENTS_ID:
                String eventId = uri.getLastPathSegment();
                synchronized (cachedEvents) {
                    Event cached = cachedEvents.get(eventId);
                    if (DEBUG) Log.i(TAG, "matched EVENTS_ID id=" + eventId + " cached=" + cached);
                    if (cached != null) {
                        LocalEvent le = new LocalEvent(getContext(), cached);
                        c.addRow(le.makeCursorRow());
                    }
                    //selection = selection + "_id = " + uri.getLastPathSegment();
                }
                break;
            case EVENTS_PRIVATE:
                synchronized (discoverEvents) {
                    for (Event e : discoverEvents) {
                        if (isEventPrivate(e)) {
                            LocalEvent me = new LocalEvent(getContext(), e);
                            c.addRow(me.makeCursorRow());
                        }
                    }
                }
                break;
            case EVENTS_DISCOVER:
                String category = uri.getLastPathSegment();
                synchronized (discoverEvents) {
                    for (Event e : discoverEvents) {
                        LocalEvent de = new LocalEvent(getContext(), e);
                        if (category.equals(de.category)) {
                            c.addRow(de.makeCursorRow());
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);


        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        return 0;
        /*
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case EVENTS:
                count = db.update(NOTES_TABLE_NAME, values, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
        */
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, EVENTS_TABLE_NAME, EVENTS);
        sUriMatcher.addURI(AUTHORITY, EVENTS_ID_PATH_MATCH, EVENTS_ID);
        sUriMatcher.addURI(AUTHORITY, EVENTS_PRIVATE_PATH, EVENTS_PRIVATE);
        sUriMatcher.addURI(AUTHORITY, EVENTS_DISCOVER_PATH_MATCH, EVENTS_DISCOVER);

        eventsProjectionMap = new HashMap<String, String>();
        eventsProjectionMap.put(Events.EVENT_ID, Events.EVENT_ID);
        eventsProjectionMap.put(Events.TITLE, Events.TITLE);
        eventsProjectionMap.put(Events.TEXT, Events.TEXT);
        eventsProjectionMap.put(Events.HOST, Events.HOST);
        eventsProjectionMap.put(Events.CATEGORY, Events.CATEGORY);
        eventsProjectionMap.put(Events.DATE, Events.DATE);
        eventsProjectionMap.put(Events.START_TIME, Events.START_TIME);
        eventsProjectionMap.put(Events.END_TIME, Events.END_TIME);
        eventsProjectionMap.put(Events.PLACE, Events.PLACE);
        eventsProjectionMap.put(Events.ADDRESS, Events.ADDRESS);
        eventsProjectionMap.put(Events.LATITUDE, Events.LATITUDE);
        eventsProjectionMap.put(Events.LONGITUDE, Events.LONGITUDE);
        eventsProjectionMap.put(Events.NUM_ATTENDEES, Events.NUM_ATTENDEES);
    }

}