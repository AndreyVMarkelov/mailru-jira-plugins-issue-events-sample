package ru.mail.jira.plugins.structs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class History
{
    private Timestamp utime;

    private int type;

    private long id;

    private int refId;

    private String user;

    private List<HistoryDetail> details;

    public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public History()
    {
        this.details = new ArrayList<HistoryDetail>();
    }

    public void addHistoryDetail(HistoryDetail hd)
    {
        this.details.add(hd);
    }

    public int getRefId() {
		return refId;
	}

	public void setRefId(int refId) {
		this.refId = refId;
	}

	public Timestamp getUtime() {
		return utime;
	}

	public void setUtime(Timestamp utime) {
		this.utime = utime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<HistoryDetail> getDetails() {
		return details;
	}

	public void setDetails(List<HistoryDetail> details) {
		this.details = details;
	}

}
