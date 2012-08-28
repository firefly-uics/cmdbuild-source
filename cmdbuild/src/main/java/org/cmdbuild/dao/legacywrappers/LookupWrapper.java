package org.cmdbuild.dao.legacywrappers;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.CMLookupType;
import org.cmdbuild.elements.Lookup;

public class LookupWrapper implements CMLookup {

	private final Lookup inner;

	public static CMLookup newInstance(final Lookup lookup) {
		if (lookup == null)
			return null;
		return new LookupWrapper(lookup);
	}

	private LookupWrapper(final Lookup inner) {
		this.inner = inner;
	}

	@Override
	public CMLookupType getType() {
		return LookupTypeWrapper.newInstance(inner.getLookupType());
	}

	@Override
	public Long getId() {
		return Long.valueOf(inner.getId());
	}

	@Override
	public CMLookup getParent() {
		return newInstance(inner.getParent());
	}

	@Override
	public String getCode() {
		return inner.getCode();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

}
