package org.cmdbuild.dao.legacywrappers;

import org.cmdbuild.dao.entrytype.CMLookupType;
import org.cmdbuild.elements.LookupType;

public class LookupTypeWrapper implements CMLookupType {

	private final LookupType inner;

	public LookupTypeWrapper(final LookupType inner) {
		this.inner = inner;
	}

	@Override
	public String getName() {
		return inner.getType();
	}


}
