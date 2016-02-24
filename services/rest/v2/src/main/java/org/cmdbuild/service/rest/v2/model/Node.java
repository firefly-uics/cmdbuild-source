package org.cmdbuild.service.rest.v2.model;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Node extends ModelWithLongId {

	private Long parent;
	private Map<String, Object> metadata;

	Node() {
		// package visibility
	}

	public Long getParent() {
		return parent;
	}

	void setParent(final Long parent) {
		this.parent = parent;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Node)) {
			return false;
		}

		final Node other = Node.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.parent, other.parent) //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(parent) //
				.append(metadata) //
				.toHashCode();
	}

}
