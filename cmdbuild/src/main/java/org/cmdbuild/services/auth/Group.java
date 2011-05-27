package org.cmdbuild.services.auth;

import org.cmdbuild.elements.interfaces.ITable;

public interface Group {

	int getId();
	String getName();
	String getDescription();
	boolean isAdmin();
	ITable getStartingClass(); 
	String[] getDisabledModules();
	boolean isDefault();
}