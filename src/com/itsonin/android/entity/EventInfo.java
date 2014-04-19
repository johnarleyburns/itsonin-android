package com.itsonin.android.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 4/19/14
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Event event;
    private Guest guest;
    private List<Guest> guests;
    private List<Comment> comments;
    private boolean viewonly;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isViewonly() {
        return viewonly;
    }

    public void setViewonly(boolean viewonly) {
        this.viewonly = viewonly;
    }

}

