package org.cmdbuild.model.data;

public class ClassOrder {

	public final String attributeName;
	public final int value;

	private ClassOrder(final String attributeName, final int value) {
		this.attributeName = attributeName;
		this.value = value;
	}

	public static ClassOrder from(final String attributeName, final int value) {
		return new ClassOrder(attributeName, value);
	}

}
