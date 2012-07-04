package org.cmdbuild.dao.legacywrappers;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.CMLookupType;
import org.cmdbuild.elements.Lookup;

public class LookupWrapper implements CMLookup {

	private final Lookup inner;

	public static CMLookup newInstance(final Lookup inner) {
		if (inner == null)
			return null;
		return new LookupWrapper(inner);
	}

	private LookupWrapper(final Lookup inner) {
		this.inner = inner;
	}

	@Override
	public CMLookupType getType() {
		return new CMLookupType() {
			@Override
			public String getName() {
				return inner.getType();
			}
		};
	}

	@Override
	public Object getId() {
		return Long.valueOf(inner.getId());
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
