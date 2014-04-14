package com.itsonin.android.entity;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.itsonin.android.enums.DeviceLevel;
import com.itsonin.android.enums.DeviceType;
import com.itsonin.android.resteasy.CustomDateTimeSerializer;

/**
 * @author nkislitsin
 *
 */
public class Device implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Long deviceId;
	private DeviceType type;
	private String token;
	private DeviceLevel level;
	private Date created;
	private Date lastLogin=new Date();

	@SuppressWarnings("unused")
	private Device(){}

	public Device(DeviceType type) {
		this.type = type;
		this.level = DeviceLevel.NORMAL;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DeviceLevel getLevel() {
		return level;
	}

	public void setLevel(DeviceLevel level) {
		this.level = level;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public Date getCreated() {
		return created;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public void setCreated(Date created) {
		this.created = created;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public Date getLastLogin() {
		return lastLogin;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

}