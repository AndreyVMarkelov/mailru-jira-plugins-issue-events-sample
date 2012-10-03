package ru.mail.jira.plugins.structs;

public class ProjectType
{
    private int id;

    private String name;

    private String comment;

	public int getId() {
		return id;
	}

    @Override
    public String toString()
    {
        return ("ProjectType[id=" + id + ", name=" + name + ", comment=" + comment + "]");
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

    
}
