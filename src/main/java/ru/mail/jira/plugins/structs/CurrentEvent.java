package ru.mail.jira.plugins.structs;

import java.util.Date;


public class CurrentEvent
{
    private String descr;

    private int ek;

    private long endDate;

    private String endTime;

    private int gp;

	private int id;

	private String name;

	private long startDate;

	private String startTime;

	public String getDescr() {
		return descr;
	}

	public int getEk() {
		return ek;
	}

	public long getEndDate() {
		return endDate;
	}

	public Date getEndDateObj() {
		return new Date(endDate);
	}

	public String getEndTime() {
		return endTime;
	}

	public int getGp() {
		return gp;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getStartDate() {
		return startDate;
	}

	public Date getStartDateObj() {
		return new Date(startDate);
	}

    public String getStartTime() {
		return startTime;
	}

    public void setDescr(String descr) {
		this.descr = descr;
	}

    public void setEk(int ek) {
		this.ek = ek;
	}

    public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setGp(int gp) {
		this.gp = gp;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	@Override
	public String toString() {
		return "CurrentEvent [name=" + name + ", descr=" + descr
				+ ", startDate=" + startDate + ", endDate=" + endDate
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", ek="
				+ ek + ", gp=" + gp + ", id=" + id + "]";
	}

}
