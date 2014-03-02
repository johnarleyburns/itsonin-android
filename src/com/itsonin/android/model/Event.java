package com.itsonin.android.model;

import android.net.Uri;
import android.provider.BaseColumns;
import com.itsonin.android.providers.EventsContentProvider;

import java.util.Random;
import java.util.UUID;

public class Event {

    public long _id;
    public String title;
    public String text;
    public String host; // name of person hosting the event
    public String category;
    public String date;
    public String startTime;
    public String endTime;
    public String place;
    public String address;
    public double latitude;
    public double longitude;
    public long numAttendees; // how many confirmed attending

    public Event() {
    }

    private static final Random mRandom = new Random();

    public static Event createTestEvent() {
        Event e = new Event();
        e._id = mRandom.nextLong();
        e.title = "Picnic in the Park";
        e.text = "Come and enjoy the wine, sunshine along the Rhine";
        e.host = "Tina";
        e.category = "picnic";
        e.date = "2014-04-01";
        e.startTime = "13:00";
        e.endTime = "19:00";
        e.place = "Rheinpark Golzheim";
        e.address = "40477 Dusseldorf, Germany";
        e.latitude = 51.243717;
        e.longitude = 6.767661;
        e.numAttendees = 7;
        return e;
    }

    public Object[] makeCursorRow() {
        return new Object[] {
                _id,
                title,
                text,
                host,
                category,
                date,
                startTime,
                endTime,
                place,
                address,
                latitude,
                longitude,
                numAttendees
        };
    }

    public static final class Events implements BaseColumns {
        private Events() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/events");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itsonin.events";

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
        public static final String NUM_ATTENDEES = "numAttendees"; // how many confirmed attending

        public static final String[] COLUMNS = {
                EVENT_ID,
                TITLE,
                TEXT,
                HOST,
                CATEGORY,
                DATE,
                START_TIME,
                END_TIME,
                PLACE,
                ADDRESS,
                LATITUDE,
                LONGITUDE,
                NUM_ATTENDEES
        };

    }

}
