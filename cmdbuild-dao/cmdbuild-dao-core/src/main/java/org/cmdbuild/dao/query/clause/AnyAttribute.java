package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;

public class AnyAttribute implements CMAttribute {

	private AnyAttribute() {
	}

	@Override
	public String getName() {
		return "*";
	}

	public static AnyAttribute anyAttribute() {
		return new AnyAttribute();
	}

	@Override
	public CMEntryType getOwner() {
		return null;
	}
}
