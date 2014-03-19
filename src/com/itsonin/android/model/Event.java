package com.itsonin.android.model;

import android.net.Uri;
import android.provider.BaseColumns;
import com.itsonin.android.providers.EventsContentProvider;

import java.util.Random;

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

    public Event(
            long _id,
            String title,
            String text,
            String host,
            String category,
            String date,
            String startTime,
            String endTime,
            String place,
            String address,
            double latitude,
            double longitude,
            long numAttendees
    ) {
        this();
        this._id = _id;
        this.title = title;
        this.text = text;
        this.host = host;
        this.category = category;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.place = place;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numAttendees = numAttendees;
    }
    
    private static final Random mRandom = new Random();

    public static Event[] createTestEvents() {
        return new Event[] {
                new Event(mRandom.nextLong(),
                        "Wednesday Pubcrawl",
                        "We'll start at Kürzer, grab some brew and a little food, " +
                                "and hit at least six places before the night's out.",
                        "Greg",
                        "going_out",
                        "Mar 12th, 2014",
                        "18:30",
                        "21:30",
                        "Brauerei Kürzer",
                        "Kurze Straße 18-20, 40213 Dusseldorf, Germany",
                        51.226987,
                        6.773343,
                        5),

                new Event(mRandom.nextLong(),
                        "Picnic in the Park this is a really long title you know",
                        "Come and enjoy the wine, sunshine along the Rhine",
                        "Tina",
                        "picnic",
                        "April 1st, 2014",
                        "13:00",
                        "19:00",
                        "Rheinpark Golzheim",
                        "40477 Dusseldorf, Germany",
                        51.243717, 6.767661,
                        7),

                new Event(mRandom.nextLong(),
                        "bare-bones beer",
                        "",
                        "",
                        "going_out",
                        "March 19th, 2014",
                        "19:00",
                        "",
                        "",
                        "",
                        0, 0,
                        0),

                new Event(mRandom.nextLong(),
                        "Cricket Picnic",
                        "We will partake of English beer, Australian wine, and perhaps, play some cricket as well.",
                        "Matt",
                        "picnic",
                        "April 7th, 2014",
                        "13:00",
                        "19:00",
                        "Nordpark",
                        "Grünewaldstraße 8, 40474 Düsseldorf, Germany",
                        51.254596, 6.748714,
                        9),

                new Event(mRandom.nextLong(),
                        "Prawns on the Barbie",
                        "Ozziefest whereby we will imbibe great quantities of Fosters while pretending the Rhine beach is a real beach.",
                        "Sergio",
                        "picnic",
                        "April 19th, 2014",
                        "16:00",
                        "22:00",
                        "Rhine Beach",
                        "47 Bremer Straße, Dusseldorf, North Rhine-Westphalia, Germany",
                        51.221575, 6.745924,
                        18),

                new Event(mRandom.nextLong(),
                        "Jogging the Hafen",
                        "Use first our legs to make a run, then later some Kolsch to make it fun.  Meet at Eigelsteins in your Addidas track suits.",
                        "John",
                        "sport",
                        "Mar 27th, 2014",
                        "19:00",
                        "21:00",
                        "Eigelstein",
                        "Hammer Straße 17, 40219 Düsseldorf, Germany",
                        51.214666,
                        6.755323,
                        4),

                new Event(mRandom.nextLong(),
                        "Insane in the Ukraine",
                        "Support our slavic brethren.  Be a firestarter.",
                        "Alexey",
                        "rally_protest",
                        "Mar 18th, 2014",
                        "12:00",
                        "13:00",
                        "Marktplatz",
                        "Marktplatz 9, 40213 Düsseldorf, Germany",
                        51.225808,
                        6.772028,
                        23),

                new Event(mRandom.nextLong(),
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
                        "Rhine Meadows",
                        "Rheinwiesen 1, 40545 Düsseldorf, Germany",
                        51.230849,
                        6.764037,
                        343),

                new Event(mRandom.nextLong(),
                        "John's 38th",
                        "Come celebrate with authentic Chinese food",
                        "John",
                        "birthday",
                        "May 24th, 2014",
                        "19:00",
                        "21:00",
                        "Xiu Chinese Restaurant",
                        "Mertensgasse 19, 40213 Dusseldorf, Germany",
                        51.226457,
                        6.773622,
                        11),

                new Event(mRandom.nextLong(),
                        "Berezovsky Piano",
                        "Schumannfest piano recital, still some tickets left, " +
                                "we'll meet in front maybe half an hour early, " +
                                "then afterwards get some drinks and discuss..",
                        "Pavel",
                        "festival",
                        "May 18th, 2014",
                        "20:00",
                        "22:00",
                        "museum kunst palast / Robert Schumann Saal",
                        "Ehrenhof 4-5, 40479 Düsseldorf",
                        51.233959, 6.771797,
                        3),

                new Event(mRandom.nextLong(),
                        "Movie Night",
                        "Come practice your Office Dinglish comprehension with us and enjoy some nachos",
                        "Ralph",
                        "watch_film",
                        "May 5th, 2014",
                        "19:30",
                        "22:00",
                        "Cinestar",
                        "Hansaallee 245, 40549 Düsseldorf, Germany",
                        51.242643, 6.719789,
                        2),

                new Event(mRandom.nextLong(),
                        "Zons Tour",
                        "Nestled between Dusseldorf and Cologne is the preserved medieval town of Zons. " +
                                "We plan to bike down and then go into one of the bars for refreshment. " +
                                "Leaving from Vodafone Prinzenallee Campus at 4.",
                        "Joerg",
                        "other",
                        "May 6th, 2014",
                        "16:00",
                        "21:00",
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

    public String toString() {
        return "Event:" + _id
                + "["
                + "title=[" + title + "]"
                + "text=[" + text + "]"
                + "host=[" + host + "]"
                + "category=[" + category + "]"
                + "date=[" + date + "]"
                + "startTime=[" + startTime + "]"
                + "endTime=[" + endTime + "]"
                + "place=[" + place + "]"
                + "address=[" + address + "]"
                + "latitude=[" + latitude + "]"
                + "longitude=[" + longitude + "]"
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
        public static final Uri EVENTS_ATTENDING_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_ATTENDING_PATH);
        public static final Uri EVENTS_HOSTING_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_HOSTING_PATH);
        public static final Uri EVENTS_INVITES_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_INVITES_PATH);
        public static final Uri EVENTS_DISCOVER_CONTENT_URI = Uri.parse("content://"
                + EventsContentProvider.AUTHORITY + "/" + EventsContentProvider.EVENTS_DISCOVER_PATH);

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
