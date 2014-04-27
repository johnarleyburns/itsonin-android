package com.itsonin.android.model;

import com.itsonin.android.controller.MainActivity;
import com.itsonin.android.enums.ItsoninWebRoot;

import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: johnarleyburns
* Date: 4/27/14
* Time: 6:19 PM
* To change this template use File | Settings | File Templates.
*/
public class ItsoninWebUri {

    public ItsoninWebRoot pathRoot;
    public long eventId;
    public long guestId;

    public ItsoninWebUri(ItsoninWebRoot pathRoot, long eventId, long guestId) {
        this.pathRoot = pathRoot;
        this.eventId = eventId;
        this.guestId = guestId;
    }

    public ItsoninWebUri(ItsoninWebRoot pathRoot) {
        this(pathRoot, 0, 0);
    }

    public String toString() {
        return "ItsoninWebUri["
                + " pathRoot=" + pathRoot
                + " eventId=" + eventId
                + " guestId=" + guestId
                + " ]"
                ;
    }

    static public ItsoninWebUri parse(List<String> pathSegments) {
        try {
            if (pathSegments == null || pathSegments.size() == 0) {
                return new ItsoninWebUri(ItsoninWebRoot.OTHER);
            }
            String pathRoot = pathSegments.get(0);
            if (ItsoninWebRoot.WELCOME.pathRoot.equals(pathRoot)) {
                return new ItsoninWebUri(ItsoninWebRoot.WELCOME);
            }
            if (ItsoninWebRoot.EVENT.pathRoot.equals(pathRoot)) {
                if (pathSegments.size() <= 1) {
                    return new ItsoninWebUri(ItsoninWebRoot.OTHER);
                }
                long eventId = Long.valueOf(pathSegments.get(1));
                if (eventId <= 0) {
                    return new ItsoninWebUri(ItsoninWebRoot.OTHER);
                }
                return new ItsoninWebUri(ItsoninWebRoot.EVENT, eventId, 0);
            }
            if (ItsoninWebRoot.INVITE.pathRoot.equals(pathRoot)) {
                if (pathSegments.size() < 2) {
                    return new ItsoninWebUri(ItsoninWebRoot.OTHER);
                }
                String[] ids = pathSegments.get(1).split("\\.");
                if (ids.length < 2) {
                    return new ItsoninWebUri(ItsoninWebRoot.OTHER);
                }
                long eventId = Long.valueOf(ids[0]);
                long guestId = Long.valueOf(ids[1]);
                return new ItsoninWebUri(ItsoninWebRoot.INVITE, eventId, guestId);
            }
        }
        catch (NumberFormatException e) {
            return new ItsoninWebUri(ItsoninWebRoot.OTHER);
        }
        return new ItsoninWebUri(ItsoninWebRoot.OTHER);
    }

}
