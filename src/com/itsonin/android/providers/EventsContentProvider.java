package com.itsonin.android.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import java.util.HashMap;
import com.itsonin.android.model.Event;
import com.itsonin.android.model.Event.Events;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/2/14
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventsContentProvider extends ContentProvider {

    private static final String TAG = EventsContentProvider.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final String AUTHORITY = "com.itsonin.android.providers.EventsContentProvider";

    private static final String EVENTS_TABLE_NAME = "events";
    private static final int EVENTS = 1;
    private static final int EVENTS_ID = 2;

    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> eventsProjectionMap;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EVENTS:
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

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        /*
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(NOTES_TABLE_NAME);
        qb.setProjectionMap(notesProjectionMap);
        */
        switch (sUriMatcher.match(uri)) {
            case EVENTS:
                break;
            case EVENTS_ID:
                //selection = selection + "_id = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        Event e = Event.createTestEvent();
        MatrixCursor c = new MatrixCursor(Events.COLUMNS, 1);
        c.addRow(e.makeCursorRow());

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
        sUriMatcher.addURI(AUTHORITY, EVENTS_TABLE_NAME + "/#", EVENTS_ID);

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