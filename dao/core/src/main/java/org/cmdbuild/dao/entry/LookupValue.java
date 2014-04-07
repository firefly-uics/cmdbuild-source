package org.cmdbuild.dao.entry;

public class LookupValue extends IdAndDescription {

	private String lookupType;

	public LookupValue( //
			final Long id, //
			final String description, //
			final String lookupType
		) {

		super(id, description);
		this.lookupType = lookupType;
	}

	public String getLooupType() {
		return lookupType;
	}

}
