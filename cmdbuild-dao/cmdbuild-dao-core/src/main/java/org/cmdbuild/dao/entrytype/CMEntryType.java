package org.cmdbuild.dao.entrytype;

public interface CMEntryType {

	public Object getId();
	public String getName();
	public String getDescription();

	public Iterable<? extends CMAttribute> getAttributes();
	public DBAttribute getAttribute(final String name);
}
