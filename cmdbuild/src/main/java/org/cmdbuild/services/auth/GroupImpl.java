package org.cmdbuild.services.auth;

import org.cmdbuild.elements.interfaces.ITable;

/*
 * Needed because isDefault is conveyed by a relation, not by the card
 */

public class GroupImpl implements Group {

	private int id;
	private String name;
	private String description;
	private boolean admin;
	private ITable startingClass;
	private boolean defaultGroup;
	private String[] disabledModules;

	public static final String SYSTEM_GROUP = "SystemGroup";
	private static final Group systemGroup = new GroupImpl(0, SYSTEM_GROUP, "System Group", true, null, true, null);

	public GroupImpl(int id, String name, String description, boolean admin, ITable startingClass, boolean defaultGroup, String[] disabledModules) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.admin = admin;
		this.startingClass = startingClass;
		this.defaultGroup = defaultGroup;
		this.disabledModules =  (disabledModules == null) ? new String[0] : disabledModules;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isDefault() {
		return defaultGroup;
	}

	public boolean isAdmin() {
		return admin;
	}

	public static Group getSystemGroup() {
		return systemGroup;
	}

	public ITable getStartingClass() {
		return startingClass;
	}

	public String toString() {
		return getName();
	}

	public String[] getDisabledModules() {		
		return disabledModules;
	}
}
