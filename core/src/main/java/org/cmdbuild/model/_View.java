package org.cmdbuild.model;

import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.services.localization.LocalizableStorable;

public interface _View extends LocalizableStorable, SerializablePrivilege {

	public enum ViewType {
		SQL, FILTER
	}

	@Override
	Long getId();

	void setId(Long id);

	@Override
	String getName();

	void setName(String name);

	@Override
	String getDescription();

	void setDescription(String description);

	String getSourceClassName();

	void setSourceClassName(String sourceClassName);

	String getSourceFunction();

	void setSourceFunction(String sourceFunction);

	String getFilter();

	void setFilter(String filter);

	ViewType getType();

	void setType(ViewType type);

	@Override
	String getIdentifier();

	@Override
	String getPrivilegeId();

}