package com.itsonin.android.entity;

import com.itsonin.android.enums.EventFlexibility;
import com.itsonin.android.enums.EventStatus;
import com.itsonin.android.enums.EventType;
import com.itsonin.android.enums.EventVisibility;
import com.itsonin.android.resteasy.CustomDateTimeSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

/**
 * @author nkislitsin
 *
 */
public class Event implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private java.lang.Long eventId;
	private EventType type;
	private EventVisibility visibility;
	private EventStatus status;
	private EventFlexibility flexibility;
	private String title;
	private String description;
	private String notes;
	private Date startTime;
	private Date endTime;
	private Double gpsLat;
	private Double gpsLong;
	private String locationUrl;
	private String locationTitle;
	private String locationAddress;
	private Date created;
	
	@SuppressWarnings("unused")
	private Event(){}

	public Event(EventType type, EventVisibility visibility, 
			EventStatus status,	EventFlexibility flexibility, String title, String description,
			String notes, Date startTime, Date endTime, Double gpsLat,Double gpsLong,
			String locationUrl, String locationTitle, String locationAddress, Date created) {
		this.type = type;
		this.visibility = visibility;
		this.status = status;
		this.flexibility = flexibility;
		this.title = title;
		this.description = description;
		this.notes = notes;
		this.startTime = startTime;
		this.endTime = endTime;
		this.gpsLat = gpsLat;
		this.gpsLong = gpsLong;
		this.locationUrl = locationUrl;
		this.locationTitle = locationTitle;
		this.locationAddress = locationAddress;
		this.created = created;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public EventVisibility getVisibility() {
		return visibility;
	}

	public void setVisibility(EventVisibility visibility) {
		this.visibility = visibility;
	}

	public EventStatus getStatus() {
		return status;
	}

	public void setStatus(EventStatus status) {
		this.status = status;
	}

	public EventFlexibility getFlexibility() {
		return flexibility;
	}

	public void setFlexibility(EventFlexibility flexibility) {
		this.flexibility = flexibility;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public java.util.Date getStartTime() {
		return startTime;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public java.util.Date getEndTime() {
		return endTime;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Double getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(Double gpsLat) {
		this.gpsLat = gpsLat;
	}

	public Double getGpsLong() {
		return gpsLong;
	}

	public void setGpsLong(Double gpsLong) {
		this.gpsLong = gpsLong;
	}

	public String getLocationUrl() {
		return locationUrl;
	}

	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}

	public String getLocationTitle() {
		return locationTitle;
	}

	public void setLocationTitle(String locationTitle) {
		this.locationTitle = locationTitle;
	}

	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(String locationAddress) {
		this.locationAddress = locationAddress;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public java.util.Date getCreated() {
		return created;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public void setCreated(Date created) {
		this.created = created;
	}


}