package org.cmdbuild.services.auth;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.model.profile.UIConfiguration;

@OldDao
@Deprecated
public interface Group {

	int getId();
	String getName();
	String getDescription();
	ITable getStartingClass(); 
	boolean isAdmin();
	boolean isCloudAdmin();
	boolean isDefault();

	/**
	 * 
	 * @return the configuration of the UI for this group
	 */
	UIConfiguration getUIConfiguration();
}