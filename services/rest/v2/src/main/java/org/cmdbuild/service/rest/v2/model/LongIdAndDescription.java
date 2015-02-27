package org.cmdbuild.service.rest.v2.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LongIdAndDescription extends AbstractModel {

	private Long id;
	private String description;

	LongIdAndDescription() {
		// package visibility
	}

	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Attribute)) {
			return false;
		}

		final LongIdAndDescription other = LongIdAndDescription.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.description, other.description) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.id) //
				.append(this.description) //
				.toHashCode();
	}

}
