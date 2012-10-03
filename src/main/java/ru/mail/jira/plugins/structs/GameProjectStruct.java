package ru.mail.jira.plugins.structs;

import java.util.ArrayList;
import java.util.List;


public class GameProjectStruct
{
    private int projectId;

    private List<EventStruct> events = new ArrayList<EventStruct>();

    private String localName;

    private int projectType;

    private int compDep;

    private int bModel;

    private String logo;

    private String page;

    private int territory;

    private String developer;

    private String origName;

    private String descr;

    public void addEvent(EventStruct e)
    {
        this.events.add(e);
    }

    public String getOrigName() {
		return origName;
	}

	public void setOrigName(String origName) {
		this.origName = origName;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public int getProjectId(EventStruct e) {
		return projectId;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public int getTerritory() {
		return territory;
	}

	public void setTerritory(int territory) {
		this.territory = territory;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public List<EventStruct> getEvents() {
		return events;
	}

	public void setEvents(List<EventStruct> events) {
		this.events = events;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public int getProjectType() {
		return projectType;
	}

	public void setProjectType(int projectType) {
		this.projectType = projectType;
	}

	public int getCompDep() {
		return compDep;
	}

	public void setCompDep(int compDep) {
		this.compDep = compDep;
	}

	public int getbModel() {
		return bModel;
	}

	public void setbModel(int bModel) {
		this.bModel = bModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + projectId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GameProjectStruct))
			return false;
		GameProjectStruct other = (GameProjectStruct) obj;
		if (projectId != other.projectId)
			return false;

		return true;
	}
}
