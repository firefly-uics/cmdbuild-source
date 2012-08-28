package org.cmdbuild.dao.legacywrappers;

import org.cmdbuild.dao.entrytype.CMLookupType;
import org.cmdbuild.elements.LookupType;

public class LookupTypeWrapper implements CMLookupType {

	private final LookupType inner;
	
	public static LookupTypeWrapper newInstance(final LookupType lookupType) {
		if (lookupType == null)
			return null;
		return new LookupTypeWrapper(lookupType);
	}

	private LookupTypeWrapper(final LookupType inner) {
		this.inner = inner;
	}

	@Override
	public String getName() {
		return inner.getType();
	}

	@Override
	public CMLookupType getParent() {
		return newInstance(inner.getParentType());
	}
}
