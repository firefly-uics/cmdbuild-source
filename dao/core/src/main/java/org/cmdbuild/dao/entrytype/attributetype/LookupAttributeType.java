package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entry.DBLookup;
import org.cmdbuild.dao.entrytype.DBLookupType;


public class LookupAttributeType extends AbstractAttributeType<CMLookup> {

	private final DBLookupType lookupType;

	public LookupAttributeType(final String lookupTypeName) {
		// TODO Get the lookup type
		this.lookupType = new DBLookupType(lookupTypeName);
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected CMLookup convertNotNullValue(Object value) {
		// TODO Get the lookup
		return new DBLookup(lookupType, value);
	}
}
