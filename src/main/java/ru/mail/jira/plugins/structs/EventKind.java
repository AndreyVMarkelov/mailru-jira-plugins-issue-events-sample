package ru.mail.jira.plugins.structs;

public class EventKind
{
    private int id;

    private String name;

    private String comment;

    private int etype;

    public EventKind() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EventKind(int id, String name, String comment, int etype) {
		this.id = id;
		this.name = name;
		this.comment = comment;
		this.etype = etype;
	}

	public int getEtype()
    {
        return etype;
    }

    public void setEtype(int etype)
    {
        this.etype = etype;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @Override
    public String toString()
    {
        return "EventKind[id=" + id + ", name=" + name + ", comment=" + comment +
            ", etype=" + etype + "]";
    }
}
