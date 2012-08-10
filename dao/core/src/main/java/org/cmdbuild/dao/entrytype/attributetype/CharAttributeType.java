package org.cmdbuild.dao.entrytype.attributetype;


public class CharAttributeType extends TextAttributeType {

	public CharAttributeType() {
		super();
	}

	@Override
	protected boolean stringLimitExceeded(final String stringValue) {
		return (stringValue.length() > 1);
	}

	private static CMAttributeType<?> daoType = new CharAttributeType();

	protected CMAttributeType<?> getDaoType() {
		return daoType;
	}
}
