package org.cmdbuild.api.fluent;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class CardDescriptor {

	private final String className;
	private final Integer id;

	private final Integer hashCode;

	public CardDescriptor(final String className, final Integer id) {
		this.className = className;
		this.id = id;

		this.hashCode = new HashCodeBuilder() //
				.append(className) //
				.append(id) //
				.hashCode();
	}

	public String getClassName() {
		return className;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof CardDescriptor)) {
			return false;
		}
		final CardDescriptor descriptor = CardDescriptor.class.cast(object);
		return (className.equals(descriptor.className) && (id == descriptor.id));
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

}
