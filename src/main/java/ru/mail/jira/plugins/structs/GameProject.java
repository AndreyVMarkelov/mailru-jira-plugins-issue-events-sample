package ru.mail.jira.plugins.structs;

public class GameProject
{
    private int bModel;

    private int compDep;

    private String descr;

    private String developer;

    private int id;

    private String localName;

    private String logo;

    private String origName;

    private String page;

    private int projType;

    private int status;

    private int territory;

    public int getbModel()
    {
        return bModel;
    }

	public int getBModel() {
		return bModel;
	}

	public int getCompDep() {
		return compDep;
	}

	public String getDescr() {
		return descr;
	}

	public String getDeveloper() {
		return developer;
	}

	public int getId() {
		return id;
	}

	public String getLocalName() {
		return localName;
	}

	public String getLogo() {
		return logo;
	}

	public String getOrigName() {
		return origName;
	}

	public String getPage() {
		return page;
	}

	public int getProjType() {
		return projType;
	}

	public int getStatus() {
		return status;
	}

    public int getTerritory()
    {
        return territory;
    }

	public void setbModel(int bModel) {
		this.bModel = bModel;
	}

	public void setBModel(int model) {
		bModel = model;
	}

	public void setCompDep(int compDep) {
		this.compDep = compDep;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public void setOrigName(String origName) {
		this.origName = origName;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public void setProjType(int projType) {
		this.projType = projType;
	}

	public void setStatus(int status) {
		this.status = status;
	}

    public void setTerritory(int territory)
    {
        this.territory = territory;
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
