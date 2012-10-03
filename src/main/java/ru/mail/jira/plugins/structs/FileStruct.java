package ru.mail.jira.plugins.structs;

public class FileStruct
{
    private int id;

    private String name;

    private String comment;

    private int eventId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	@Override
	public String toString() {
		return "FileStruct [id=" + id + ", name=" + name + ", comment="
				+ comment + ", eventId=" + eventId + "]";
	}
}
