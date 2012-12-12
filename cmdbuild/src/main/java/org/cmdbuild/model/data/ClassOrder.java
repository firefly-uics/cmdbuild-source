package org.cmdbuild.model.data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ClassOrder {

	public final String attributeName;
	public final int value;

	private ClassOrder(final String attributeName, final int value) {
		this.attributeName = attributeName;
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		return attributeName.equals(obj);
	}

	@Override
	public int hashCode() {
		return attributeName.hashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public static ClassOrder from(final String attributeName, final int value) {
		return new ClassOrder(attributeName, value);
	}

}
