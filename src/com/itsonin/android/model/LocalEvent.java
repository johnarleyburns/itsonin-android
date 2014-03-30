package com.itsonin.android.model;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;
import com.itsonin.android.R;
import com.itsonin.android.entity.Event;
import com.itsonin.android.entity.EventWithGuest;
import com.itsonin.android.entity.Guest;
import com.itsonin.android.enums.EventFlexibility;
import com.itsonin.android.enums.EventStatus;
import com.itsonin.android.enums.EventType;
import com.itsonin.android.enums.EventVisibility;
import com.itsonin.android.providers.EventsContentProvider;
import com.itsonin.android.resteasy.CustomDateTimeSerializer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class LocalEvent {

    private static final String TAG = LocalEvent.class.getSimpleName();

    public long _id;
    public String title;
    public String description;
    public String host; // name of person hosting the event
    public String category;
    public String date;
    public String startTime;
    public String endTime;
    public boolean privateEvent = true;
    public String locationTitle;
    public String locationAddress;
    public double gpsLat;
    public double gpsLong;
    public long numAttendees; // how many confirmed attending

    public LocalEvent() {
    }

    public LocalEvent(
            long _id,
            String title,
            String description,
            String host,
            String category,
            String date,
            String startTime,
            String endTime,
            boolean privateEvent,
            String locationTitle,
            String locationAddress,
            double gpsLat,
            double gpsLong,
            long numAttendees
    ) {
        this();
        this._id = _id;
        this.title = title;
        this.description = description;
        this.host = host;
        this.category = category;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.privateEvent = privateEvent;
        this.locationTitle = locationTitle;
        this.locationAddress = locationAddress;
        this.gpsLat = gpsLat;
        this.gpsLong = gpsLong;
        this.numAttendees = numAttendees;
    }

    public LocalEvent(Context c, Event e) {
        this();
        this._id = e.getEventId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.host = null;
        this.category = mapEventType(e.getType());
        this.date = friendlyDate(c, e.getStartTime());
        this.startTime = friendlyTime(c, e.getStartTime());
        this.endTime = friendlyTime(c, e.getEndTime());
        this.privateEvent = mapVisibility(e.getVisibility());
        this.locationTitle = e.getLocationTitle();
        this.locationAddress = e.getLocationAddress();
        this.gpsLat = e.getGpsLat();
        this.gpsLong = e.getGpsLong();
        this.numAttendees = 0;
    }

    private static boolean isTomorrow(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        return DateUtils.isToday(c.getTime().getTime());
    }

    private static boolean isCurrentWeek(Date date) {
        Calendar c = Calendar.getInstance();
        int currentWeek = c.get(Calendar.WEEK_OF_YEAR);
        c.setTime(date);
        int dateWeek = c.get(Calendar.WEEK_OF_YEAR);
        return currentWeek == dateWeek;
    }

    private static boolean isCurrentYear(Date date) {
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        c.setTime(date);
        int dateYear = c.get(Calendar.YEAR);
        return currentYear == dateYear;
    }

    private static final String DATE_FORMAT_DAY_OF_WEEK = "EEEE";
    private static final String DATE_FORMAT_THIS_YEAR = "EEE, MMM d";

    public static String friendlyDate(Context context, Date date) { // no time component
        String s;
        if (DateUtils.isToday(date.getTime())) {
            s = context.getString(R.string.today);
        }
        else if (isTomorrow(date)) {
            s = context.getString(R.string.tomorrow);
        }
        else if (isCurrentWeek(date)) {
            s = new SimpleDateFormat(DATE_FORMAT_DAY_OF_WEEK).format(date);
        }
        else if (isCurrentYear(date)) {
            s = new SimpleDateFormat(DATE_FORMAT_THIS_YEAR).format(date);
        }
        else {
            s = DateFormat.getDateInstance().format(date);
        }
        return s;
    }

    public static String friendlyTime(Context context, Date time) {
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        return df.format(time);
    }

    private static final Random mRandom = new Random();

    public static LocalEvent[] createTestEvents() {
        return new LocalEvent[] {
                new LocalEvent(mRandom.nextLong(),
                        "Wednesday Pubcrawl",
                        "We'll start at Kürzer, grab some brew and a little food, " +
                                "and hit at least six places before the night's out.",
                        "Greg",
                        "going_out",
                        "Mar 12th, 2014",
                        "18:30",
                        "21:30",
                        false,
                        "Brauerei Kürzer",
                        "Kurze Straße 18-20, 40213 Dusseldorf, Germany",
                        51.226987,
                        6.773343,
                        5),

                new LocalEvent(mRandom.nextLong(),
                        "Picnic in the Park this is a really long title you know",
                        "Come and enjoy the wine, sunshine along the Rhine",
                        "Tina",
                        "picnic",
                        "April 1st, 2014",
                        "13:00",
                        "19:00",
                        false,
                        "Rheinpark Golzheim",
                        "40477 Dusseldorf, Germany",
                        51.243717, 6.767661,
                        7),

                new LocalEvent(mRandom.nextLong(),
                        "bare-bones beer",
                        "",
                        "",
                        "going_out",
                        "March 19th, 2014",
                        "19:00",
                        "",
                        false,
                        "",
                        "",
                        0, 0,
                        0),

                new LocalEvent(mRandom.nextLong(),
                        "Cricket Picnic",
                        "We will partake of English beer, Australian wine, and perhaps, play some cricket as well.",
                        "Matt",
                        "picnic",
                        "April 7th, 2014",
                        "13:00",
                        "19:00",
                        false,
                        "Nordpark",
                        "Grünewaldstraße 8, 40474 Düsseldorf, Germany",
                        51.254596, 6.748714,
                        9),

                new LocalEvent(mRandom.nextLong(),
                        "Prawns on the Barbie",
                        "Ozziefest whereby we will imbibe great quantities of Fosters while pretending the Rhine beach is a real beach.",
                        "Sergio",
                        "picnic",
                        "April 19th, 2014",
                        "16:00",
                        "22:00",
                        false,
                        "Rhine Beach",
                        "47 Bremer Straße, Dusseldorf, North Rhine-Westphalia, Germany",
                        51.221575, 6.745924,
                        18),

                new LocalEvent(mRandom.nextLong(),
                        "Jogging the Hafen",
                        "Use first our legs to make a run, then later some Kolsch to make it fun.  Meet at Eigelsteins in your Addidas track suits.",
                        "John",
                        "sport",
                        "Mar 27th, 2014",
                        "19:00",
                        "21:00",
                        false,
                        "Eigelstein",
                        "Hammer Straße 17, 40219 Düsseldorf, Germany",
                        51.214666,
                        6.755323,
                        4),

                new LocalEvent(mRandom.nextLong(),
                        "Insane in the Ukraine",
                        "Support our slavic brethren.  Be a firestarter.",
                        "Alexey",
                        "rally_protest",
                        "Mar 18th, 2014",
                        "12:00",
                        "13:00",
                        false,
                        "Marktplatz",
                        "Marktplatz 9, 40213 Düsseldorf, Germany",
                        51.225808,
                        6.772028,
                        23),

                new LocalEvent(mRandom.nextLong(),
                        "Flashmob Rhine",
                        "Take the U-Bahn to Luegplatz, get out on the south side, walk down by the Rhine. " +
                                "Bring a helium balloon, we're all going to release balloons at once exactly at 12:00. " +
                                "We have people on the other side of the river who will photograph the whole thing. " +
                                "Be there exactly at 12:00 because we will release and disperse in only a minute total " +
                                "to avoid suspicion.",
                        "Anonymous",
                        "mischief",
                        "Mar 12th, 2014",
                        "11:59",
                        "12:01",
                        false,
                        "Rhine Meadows",
                        "Rheinwiesen 1, 40545 Düsseldorf, Germany",
                        51.230849,
                        6.764037,
                        343),

                new LocalEvent(mRandom.nextLong(),
                        "John's 38th",
                        "Come celebrate with authentic Chinese food",
                        "John",
                        "birthday",
                        "May 24th, 2014",
                        "19:00",
                        "21:00",
                        false,
                        "Xiu Chinese Restaurant",
                        "Mertensgasse 19, 40213 Dusseldorf, Germany",
                        51.226457,
                        6.773622,
                        11),

                new LocalEvent(mRandom.nextLong(),
                        "Berezovsky Piano",
                        "Schumannfest piano recital, still some tickets left, " +
                                "we'll meet in front maybe half an hour early, " +
                                "then afterwards get some drinks and discuss..",
                        "Pavel",
                        "festival",
                        "May 18th, 2014",
                        "20:00",
                        "22:00",
                        false,
                        "museum kunst palast / Robert Schumann Saal",
                        "Ehrenhof 4-5, 40479 Düsseldorf",
                        51.233959, 6.771797,
                        3),

                new LocalEvent(mRandom.nextLong(),
                        "Movie Night",
                        "Come practice your Office Dinglish comprehension with us and enjoy some nachos",
                        "Ralph",
                        "watch_film",
                        "May 5th, 2014",
                        "19:30",
                        "22:00",
                        false,
                        "Cinestar",
                        "Hansaallee 245, 40549 Düsseldorf, Germany",
                        51.242643, 6.719789,
                        2),

                new LocalEvent(mRandom.nextLong(),
                        "Zons Tour",
                        "Nestled between Dusseldorf and Cologne is the preserved medieval town of Zons. " +
                                "We plan to bike down and then go into one of the bars for refreshment. " +
                                "Leaving from Vodafone Prinzenallee Campus at 4.",
                        "Joerg",
                        "other",
                        "May 6th, 2014",
                        "16:00",
                        "21:00",
                        false,
                        "Zons",
                        "Nievenheimer Straße 7, 41541 Dormagen, Germany",
                        51.123135, 6.843747,
                        4)

        };
    }

    public Object[] makeCursorRow() {
        return new Object[] {
                _id,
                title,
                description,
                host,
                category,
                date,
                startTime,
                endTime,
                locationTitle,
                locationAddress,
                gpsLat,
                gpsLong,
                numAttendees
        };
    }

    public String toString() {
        return "Event:" + _id
                + "["
                + "title=[" + title + "]"
                + "description=[" + description + "]"
                + "host=[" + host + "]"
                + "category=[" + category + "]"
                + "date=[" + date + "]"
                + "startTime=[" + startTime + "]"
                + "endTime=[" + endTime + "]"
                + "privateEvent=[" + privateEvent + "]"
                + "locationTitle=[" + locationTitle + "]"
                + "locationAddress=[" + locationAddress + "]"
                + "gpsLat=[" + gpsLat + "]"
                + "gpsLong=[" + gpsLong + "]"
                + "numAttendees=[" + numAttendees + "]"
                + "]";
    }

    public static final class Events implements BaseColumns {
        private Events() {
        }

        public static final Uri EVENTS_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_TABLE_NAME);
        public static final Uri EVENTS_ID_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_ID_PATH);
        public static final Uri EVENTS_PRIVATE_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_PRIVATE_PATH);
        public static final Uri EVENTS_DISCOVER_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_DISCOVER_PATH);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itsonin.events";

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

    public Event toEvent(Context context) {
        return new Event(
                mapCategory(),
                mapPrivate(),
                EventStatus.ACTIVE,
                EventFlexibility.NEGOTIABLE,
                title,
                description,
                null,
                mapStartDate(context),
                mapEndDate(context),
                gpsLat,
                gpsLong,
                null,
                locationTitle,
                locationAddress,
                new Date()
        );
    }

    private String mapEventType(EventType e) {
        switch (e) {
            case PICNIC:
                return "picnic";
            case PARTY:
                return "going_out";
            case EXCERCISE:
                return "sport";
            case FLASHMOB:
                return "mischief";
            case PROTEST:
                return "rally_protest";
            case RALLY:
                return "rally_protest";
            default:
                return "other";
        }
    }

    private EventType mapCategory() {
        if ("picnic".equals(category)) {
            return EventType.PICNIC;
        }
        else if ("going_out".equals(category)) {
            return EventType.PARTY;
        }
        else if ("rally_protest".equals(category)) {
            return EventType.RALLY;
        }
        else if ("birthday".equals(category)) {
            return EventType.PARTY;
        }
        else if ("festival".equals(category)) {
            return EventType.PARTY;
        }
        else if ("mischief".equals(category)) {
            return EventType.FLASHMOB;
        }
        else if ("sport".equals(category)) {
            return EventType.EXCERCISE;
        }
        else if ("watch_film".equals(category)) {
            return EventType.PARTY;
        }
        else if ("other".equals(category)) {
            return EventType.PARTY;
        }
        else {
            return EventType.PARTY;
        }
    }

    private EventVisibility mapPrivate() {
        if (privateEvent) {
            return EventVisibility.PRIVATE;
        }
        else {
            return EventVisibility.PUBLIC;
        }
    }

    private boolean mapVisibility(EventVisibility v) {
        switch (v) {
            case PUBLIC:
                return false;
            case PRIVATE:
            case FRIENDSONLY:
            default:
                return true;
        }
    }

    private Date mapStartDate(Context context) {
        return mapDateWithTime(context, date, startTime);
    }

    private Date mapEndDate(Context context) {
        return mapDateWithTime(context, date, endTime != null && !endTime.isEmpty() ? endTime : startTime);
    }

    private Date mapDateWithTime(Context context, String date, String time) {
        try {
            Date d = new SimpleDateFormat(CustomDateTimeSerializer.ITSONIN_DATES).parse(date);
            Date t = android.text.format.DateFormat.getTimeFormat(context).parse(time);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(d);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(t);
            c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
            c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
            return c1.getTime();
        }
        catch (ParseException e) {
            Log.e(TAG, "Exception mapping date", e);
            return new Date();
        }
    }

    public Guest toHostGuest() {
        return new Guest(host);
    }

    public EventWithGuest toEventWithGuest(Context context) {
        return new EventWithGuest(toEvent(context), toHostGuest());
    }

}
