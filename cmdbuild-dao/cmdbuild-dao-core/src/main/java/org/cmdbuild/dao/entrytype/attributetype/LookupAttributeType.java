package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entry.DBLookup;


public class LookupAttributeType implements CMAttributeType<CMLookup> {

	public LookupAttributeType() {
	}

	@Override
	public CMLookup convertNotNullValue(Object value) {
		// TODO Conversion of value?!
		return new DBLookup(value);
	}
}
