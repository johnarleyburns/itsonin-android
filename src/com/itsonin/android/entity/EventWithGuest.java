package com.itsonin.android.entity;

import java.io.Serializable;

/**
 * @author nkislitsin
 *
 */
public class EventWithGuest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Event event;
	private Guest guest;
	
	public EventWithGuest() {
	}
	
	public EventWithGuest(Event event, Guest guest) {
		this.event = event;
		this.guest = guest;
	}

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

}
