package org.cmdbuild.portlet.metadata;

public class User {

	private int id;
	private String name;
	private String login;
	private String group;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(final String login) {
		this.login = login;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(final String group) {
		this.group = group;
	}

}
