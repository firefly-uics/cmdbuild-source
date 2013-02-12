package org.cmdbuild.dao.entrytype.attributetype;

public class StringArrayAttributeType extends AbstractAttributeType<String[]> {

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected String[] convertNotNullValue(Object value) {
		if (!(value instanceof String[])) {
			throw new IllegalArgumentException();
		}

		return (String[]) value;
	}

}
