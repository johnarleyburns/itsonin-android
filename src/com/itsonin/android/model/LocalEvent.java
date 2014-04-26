package com.itsonin.android.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;
import com.itsonin.android.R;
import com.itsonin.android.entity.*;
import com.itsonin.android.enums.*;
import com.itsonin.android.providers.EventsContentProvider;
import com.itsonin.android.resteasy.CustomDateTimeSerializer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LocalEvent {

    private static final String TAG = LocalEvent.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String SHARE_URL_PATTERN = "http://itsonin-com.appspot.com/i/%1$s.%2$s"; // eventId, guestId

    public long _id;
    public String status;
    public String title;
    public String description;
    public String category;
    public String sharability;
    public String visibility;
    public String startTimeRaw;
    public String endTimeRaw;
    public String date;
    public String startTime;
    public String endTime;
    public boolean privateEvent = true;
    public String locationTitle;
    public String locationAddress;
    public double gpsLat;
    public double gpsLong;
    public long numAttendees; // how many confirmed attending
    public long numComments;
    public String attendingText;
    public String commentsText;
    public String shareUrl;
    public boolean viewOnly;
    public String guestName;
    public String guestStatus;
    public long guestDeviceId;
    public long guestGuestId;
    public String guestType;
    public String guestCreated;

    public LocalEvent() {
    }

    public LocalEvent(Context c, Event e) {
        this();
        _id = e.getEventId();
        status = e.getStatus().toString();
        title = e.getTitle();
        description = e.getDescription();
        category = mapEventType(e.getType());
        sharability = e.getSharability().toString();
        visibility = e.getVisibility().toString();
        startTimeRaw = new CustomDateTimeSerializer().format(e.getStartTime());
        endTimeRaw = new CustomDateTimeSerializer().format(e.getEndTime());
        date = friendlyDate(c, e.getStartTime());
        startTime = friendlyTime(c, e.getStartTime());
        endTime = friendlyTime(c, e.getEndTime());
        privateEvent = mapVisibility(e.getVisibility());
        locationTitle = e.getLocationTitle();
        locationAddress = e.getLocationAddress();
        gpsLat = e.getGpsLat() == null ? 0 : e.getGpsLat();
        gpsLong = e.getGpsLong() == null ? 0 : e.getGpsLong();
        numAttendees = 0;
        numComments = 0;
        attendingText = null;
        commentsText = null;
        shareUrl = null;
        viewOnly = true;
        guestName = null;
        guestStatus = null;
        guestDeviceId = 0;
        guestGuestId = 0;
        guestType = null;
        guestCreated = null;
    }

    public LocalEvent(Context c, Cursor cursor) {
        this();
        _id = cursor.getLong(cursor.getColumnIndex(Events.EVENT_ID));
        status = cursor.getString(cursor.getColumnIndex(Events.STATUS));
        title = cursor.getString(cursor.getColumnIndex(Events.TITLE));
        description = cursor.getString(cursor.getColumnIndex(Events.TEXT));
        sharability = cursor.getString(cursor.getColumnIndex(Events.SHARABILITY));
        visibility = cursor.getString(cursor.getColumnIndex(Events.VISIBILITY));
        startTimeRaw = cursor.getString(cursor.getColumnIndex(Events.START_TIME_RAW));
        endTimeRaw = cursor.getString(cursor.getColumnIndex(Events.END_TIME_RAW));
        date = cursor.getString(cursor.getColumnIndex(Events.DATE));
        startTime = cursor.getString(cursor.getColumnIndex(Events.START_TIME));
        endTime = cursor.getString(cursor.getColumnIndex(Events.END_TIME));
        locationTitle = cursor.getString(cursor.getColumnIndex(Events.LOCATION_TITLE));
        locationAddress = cursor.getString(cursor.getColumnIndex(Events.LOCATION_ADDRESS));
        gpsLat = cursor.getDouble(cursor.getColumnIndex(Events.LATITUDE));
        gpsLong = cursor.getDouble(cursor.getColumnIndex(Events.LONGITUDE));
        numAttendees = cursor.getInt(cursor.getColumnIndex(Events.NUM_ATTENDEES));
        numComments = cursor.getInt(cursor.getColumnIndex(Events.NUM_COMMENTS));
        attendingText = cursor.getString(cursor.getColumnIndex(Events.ATTENDING_TEXT));
        commentsText = cursor.getString(cursor.getColumnIndex(Events.COMMENTS_TEXT));
        shareUrl = cursor.getString(cursor.getColumnIndex(Events.SHARE_URL));
        viewOnly = cursor.getInt(cursor.getColumnIndex(Events.VIEWONLY)) == 1;
        guestName = cursor.getString(cursor.getColumnIndex(Events.GUEST_NAME));
        guestStatus = cursor.getString(cursor.getColumnIndex(Events.GUEST_STATUS));
        guestDeviceId = cursor.getLong(cursor.getColumnIndex(Events.GUEST_DEVICE_ID));
        guestGuestId = cursor.getLong(cursor.getColumnIndex(Events.GUEST_GUEST_ID));
        guestType = cursor.getString(cursor.getColumnIndex(Events.GUEST_TYPE));
        guestCreated = cursor.getString(cursor.getColumnIndex(Events.GUEST_CREATED));
    }

    public String mapGuestType(Context c, GuestType type) {
        switch(type) {
            case HOST:
                return c.getString(R.string.guest_host);
            case GUEST:
            default:
                return "";
        }
    }

    public String mapGuestStatus(Context c, GuestStatus status) {
        switch (status) {
            case ATTENDING:
                return c.getString(R.string.guest_attending_short);
            case DECLINED:
                return c.getString(R.string.guest_declined_short);
            case PENDING:
                return c.getString(R.string.guest_pending_short);
            case VIEWED:
            default:
                return "";
        }
    }

    public LocalEvent(Context c, EventInfo eventInfo) {
        this(c, eventInfo.getEvent());

        List<Guest> guests = eventInfo.getGuests();
        Collections.sort(guests, new Comparator<Guest>() {
            @Override
            public int compare(Guest lhs, Guest rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        
        StringBuilder guestBuilder = new StringBuilder();
        for (Guest guest : guests) {
            if (guest.getStatus() == GuestStatus.VIEWED) {
                continue;
            }
            if (guest.getStatus() == GuestStatus.ATTENDING) {
                numAttendees++;
            }
            if (guestBuilder.length() > 0) {
                guestBuilder.append("<br />");
            }
            guestBuilder.append(guest.getName());
            guestBuilder.append(" <b>");
            if (guest.getType() == GuestType.HOST) {
                guestBuilder.append(mapGuestType(c, guest.getType()));
            }
            else {
                guestBuilder.append(mapGuestStatus(c, guest.getStatus()));
            }
            guestBuilder.append("</b>");
        }
        attendingText = guestBuilder.toString();
        
        List<Comment> comments = eventInfo.getComments();
        Collections.sort(comments, new Comparator<Comment>() {
            @Override
            public int compare(Comment lhs, Comment rhs) {
                return lhs.getCreated().compareTo(rhs.getCreated());
            }
        });

        StringBuilder commentBuilder = new StringBuilder();
        for (Comment comment: comments) {
            numComments++;
            if (commentBuilder.length() > 0) {
                commentBuilder.append("<br />");
            }
            commentBuilder.append("<b>")
                    .append(comment.getCreated().toString())
                    .append("</b> ")
                    .append(comment.getComment());
        }
        commentsText = commentBuilder.toString();

        Guest guest = eventInfo.getGuest();
        long guestId;
        if (guest.getStatus() == GuestStatus.PENDING) {
            guestId = guest.getParentGuestId();
        }
        else if (guest.getStatus() == GuestStatus.ATTENDING
                || guest.getStatus() == GuestStatus.DECLINED) {
            guestId = guest.getGuestId();
        }
        else {
            guestId = -1;
        }
        if (guestId >= 0) {
            shareUrl = String.format(SHARE_URL_PATTERN, _id, guestId);
        }
        else {
            shareUrl = null;
        }
        guestName = guest.getName();
        guestStatus = guest.getStatus().toString();
        guestDeviceId = guest.getDeviceId();
        guestGuestId = guestId;
        guestType = guest.getType().toString();
        guestCreated = new CustomDateTimeSerializer().format(guest.getCreated());

        viewOnly = eventInfo.isViewonly();
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
        if (date == null) {
            return null;
        }
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
        if (time == null) {
            return null;
        }
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        return df.format(time);
    }

    private static final Random mRandom = new Random();

    /*
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
    */

    public Object[] makeCursorRow() {
        if (DEBUG) Log.i(TAG, "makeCursorRow() " + toString());
        return new Object[] {
                _id,
                status,
                title,
                description,
                category,
                sharability,
                visibility,
                startTimeRaw,
                endTimeRaw,
                date,
                startTime,
                endTime,
                locationTitle,
                locationAddress,
                gpsLat,
                gpsLong,
                numAttendees,
                numComments,
                attendingText,
                commentsText,
                shareUrl,
                viewOnly ? 1 : 0,
                guestName,
                guestStatus,
                guestDeviceId,
                guestGuestId,
                guestType,
                guestCreated
        };
    }

    public String toString() {
        return "Event:" + _id
                + "["
                + "status=[" + status + "]"
                + "title=[" + title + "]"
                + "description=[" + description + "]"
                + "category=[" + category + "]"
                + "sharability=[" + sharability + "]"
                + "startTimeRaw=[" + startTimeRaw + "]"
                + "endTimeRaw=[" + endTimeRaw + "]"
                + "date=[" + date + "]"
                + "startTime=[" + startTime + "]"
                + "endTime=[" + endTime + "]"
                + "privateEvent=[" + privateEvent + "]"
                + "locationTitle=[" + locationTitle + "]"
                + "locationAddress=[" + locationAddress + "]"
                + "gpsLat=[" + gpsLat + "]"
                + "gpsLong=[" + gpsLong + "]"
                + "numAttendees=[" + numAttendees + "]"
                + "numComments=[" + numComments + "]"
                + "attendingText=[" + attendingText + "]"
                + "commentsText=[" + commentsText + "]"
                + "shareUrl=[" + shareUrl + "]"
                + "viewOnly=[" + viewOnly + "]"
                + "guestName=[" + guestName + "]"
                + "guestStatus=[" + guestStatus + "]"
                + "guestDeviceId=[" + guestDeviceId + "]"
                + "guestGuestId=[" + guestGuestId + "]"
                + "guestType=[" + guestType + "]"
                + "guestCreated=[" + guestCreated + "]"
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
        public static final String STATUS = "status";
        public static final String TITLE = "title";
        public static final String TEXT = "description";
        public static final String CATEGORY = "category";
        public static final String SHARABILITY = "sharability";
        public static final String VISIBILITY = "visibility";
        public static final String START_TIME_RAW = "startTimeRaw";
        public static final String END_TIME_RAW = "endTimeRaw";
        public static final String DATE = "date";
        public static final String START_TIME = "startTime";
        public static final String END_TIME = "endTime";
        public static final String LOCATION_TITLE = "locationTitle";
        public static final String LOCATION_ADDRESS = "locationAddress";
        public static final String LATITUDE = "gpsLat";
        public static final String LONGITUDE = "gpsLong";
        public static final String NUM_ATTENDEES = "numAttendees"; // how many confirmed attending
        public static final String NUM_COMMENTS = "numComments"; // how many confirmed attending
        public static final String ATTENDING_TEXT = "attendingText"; // how many confirmed attending
        public static final String COMMENTS_TEXT = "commentsText"; // how many confirmed attending
        public static final String SHARE_URL = "shareUrl";
        public static final String VIEWONLY = "viewOnly";
        public static final String GUEST_NAME = "guestName";
        public static final String GUEST_STATUS = "guestStatus";
        public static final String GUEST_DEVICE_ID = "guestDeviceId";
        public static final String GUEST_GUEST_ID = "guestGuestId";
        public static final String GUEST_TYPE = "guestType";
        public static final String GUEST_CREATED = "guestCreated";

        public static final String[] COLUMNS = {
                EVENT_ID,
                STATUS,
                TITLE,
                TEXT,
                CATEGORY,
                SHARABILITY,
                VISIBILITY,
                START_TIME_RAW,
                END_TIME_RAW,
                DATE,
                START_TIME,
                END_TIME,
                LOCATION_TITLE,
                LOCATION_ADDRESS,
                LATITUDE,
                LONGITUDE,
                NUM_ATTENDEES,
                NUM_COMMENTS,
                ATTENDING_TEXT,
                COMMENTS_TEXT,
                SHARE_URL,
                VIEWONLY,
                GUEST_NAME,
                GUEST_STATUS,
                GUEST_DEVICE_ID,
                GUEST_GUEST_ID,
                GUEST_TYPE,
                GUEST_CREATED
        };

    }

    public Event toEvent(Context context) {
        Event e = new Event(
                mapCategory(),
                mapSharability(),
                mapEventVisibility(),
                mapEventStatus(),
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
        if (_id >= 0) {
            e.setEventId(_id);
        }
        return e;
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

    private EventSharability mapSharability() {
        if (sharability == null) {
            return EventSharability.NORMAL;
        }
        EventSharability e = EventSharability.valueOf(sharability);
        if (e == null) {
            return EventSharability.NORMAL;
        }
        else {
            return e;
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

    private EventVisibility mapEventVisibility() {
        EventVisibility v = visibility == null ? null : EventVisibility.valueOf(visibility);
        if (v != null) {
            return v;
        }
        else {
            return EventVisibility.PRIVATE;
        }
    }

    private EventStatus mapEventStatus() {
        EventStatus s = status == null ? null : EventStatus.valueOf(status);
        if (s != null) {
            return s;
        }
        else {
            return EventStatus.ACTIVE;
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
        if (date == null || time == null) {
            Log.e(TAG, "mapDateWithTime() null date=" + date + " time=" + time);
            return new Date();
        }
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
            Log.e(TAG, "Exception mapping date with time", e);
            return new Date();
        }
    }

    public Guest toGuest() {
        return new Guest(
                guestDeviceId,
                guestGuestId,
                _id,
                guestName,
                guestType == null ? null : GuestType.valueOf(guestType),
                guestStatus == null ? null : GuestStatus.valueOf(guestStatus),
                guestCreated == null ? null : new CustomDateTimeSerializer().parse(guestCreated)
        );
    }

    public EventWithGuest toEventWithGuest(Context context) {
        return new EventWithGuest(toEvent(context), toGuest());
    }

}
