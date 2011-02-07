package org.cmdbuild.portlet.configuration;

public class CardConfiguration {

	private String classname;
	private String classdescription;
	private String type;
	private String flowstatus;
	private int id;
	private String privilege;
	private boolean displayNotes;

	public String getClassdescription() {
		return classdescription;
	}

	public void setClassdescription(final String classdescription) {
		this.classdescription = classdescription;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(final String classname) {
		this.classname = classname;
	}

	public String getFlowstatus() {
		return flowstatus;
	}

	public void setFlowstatus(final String flowstatus) {
		this.flowstatus = flowstatus;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(final String privilege) {
		this.privilege = privilege;
	}

	public boolean isDisplayNotes() {
		return displayNotes;
	}

	public void setDisplayNotes(final boolean displayNotes) {
		this.displayNotes = displayNotes;
	}

}
