package com.itsonin.android.entity;

/**
 * @author nkislitsin
 *
 */
public class GuestDevice {

	private java.lang.String id;
	private Long deviceId;
	private Long guestId;
	
	@SuppressWarnings("unused")
	private GuestDevice(){}

	public GuestDevice(Long deviceId, Long guestId) {
		this.id = deviceId + "_" + guestId;
		this.deviceId = deviceId;
		this.guestId = guestId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Long getGuestId() {
		return guestId;
	}

	public void setGuestId(Long guestId) {
		this.guestId = guestId;
	}


}