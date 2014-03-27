package com.itsonin.android.entity;

import com.itsonin.android.enums.GuestType;
import com.itsonin.android.resteasy.CustomDateTimeSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;

/**
 * @author nkislitsin
 *
 */
public class Guest {

	private java.lang.String id;
	private Long guestId;
	private Long eventId;
	private Long parentGuestId;
	private String name;
	private GuestType type;
	private Date created;
	
	@SuppressWarnings("unused")
	private Guest(){}
	
	public Guest(String name) {
		this.name = name;
	}

	public Guest(Long guestId, Long eventId, String name, GuestType type, Date created) {
		this.id = eventId + "_" + guestId;
		this.guestId = guestId;
		this.eventId = eventId;
		this.name = name;
		this.type = type;
		this.created = created;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getGuestId() {
		return guestId;
	}

	public void setGuestId(Long guestId) {
		this.guestId = guestId;
	}
	
	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getParentGuestId() {
		return parentGuestId;
	}

	public void setParentGuestId(Long parentGuestId) {
		this.parentGuestId = parentGuestId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GuestType getType() {
		return type;
	}

	public void setType(GuestType type) {
		this.type = type;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public java.util.Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}