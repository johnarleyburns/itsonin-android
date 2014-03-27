package com.itsonin.android.entity;

import com.itsonin.android.resteasy.CustomDateTimeSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;

/**
 * @author nkislitsin
 *
 */
public class Comment {

	private java.lang.Long commentId;
	private Long eventId;
	private Long guestId;
	private Long parentCommentId;
	private String comment;
	private Date created;
	
	@SuppressWarnings("unused")
	private Comment(){}

	public Comment(Long eventId, Long guestId,
			Long parentCommentId, String comment, Date created) {
		this.eventId = eventId;
		this.guestId = guestId;
		this.parentCommentId = parentCommentId;
		this.comment = comment;
		this.created = created;
	}

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getGuestId() {
		return guestId;
	}

	public void setGuestId(Long guestId) {
		this.guestId = guestId;
	}

	public Long getParentCommentId() {
		return parentCommentId;
	}

	public void setParentCommentId(Long parentCommentId) {
		this.parentCommentId = parentCommentId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public java.util.Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}