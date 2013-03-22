package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class DBLookupType implements CMLookupType {

	private final String name;
	private final transient String toString;

	public DBLookupType(final String name) {
		this.name = name;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CMLookupType getParent() {
		// TODO
		return null;
	}
	
	@Override
	public String toString() {
		return toString;
	}

}
