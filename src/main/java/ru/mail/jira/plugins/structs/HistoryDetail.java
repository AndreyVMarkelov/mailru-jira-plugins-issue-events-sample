package ru.mail.jira.plugins.structs;

public class HistoryDetail
{
    private String field;

    private String oldval;

    private String newval;

    public HistoryDetail() {}

    public HistoryDetail(String field, String oldval, String newval)
    {
        this.field = field;
        this.oldval = oldval;
        this.newval = newval;
    }

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getOldval() {
		return oldval;
	}

	public void setOldval(String oldval) {
		this.oldval = oldval;
	}

	public String getNewval() {
		return newval;
	}

	public void setNewval(String newval) {
		this.newval = newval;
	}

	@Override
	public String toString() {
		return "HistoryDetail [field=" + field + ", oldval=" + oldval
				+ ", newval=" + newval + "]";
	}
}
