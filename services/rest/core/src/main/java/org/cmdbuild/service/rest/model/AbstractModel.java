package org.cmdbuild.service.rest.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class AbstractModel {

	protected AbstractModel() {
		// usable by subclasses only
	}

	@Override
	public boolean equals(final Object obj) {
		return doEquals(obj);
	}

	protected abstract boolean doEquals(Object obj);

	@Override
	public int hashCode() {
		return doHashCode();
	}

	protected abstract int doHashCode();

	@Override
	public final String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}