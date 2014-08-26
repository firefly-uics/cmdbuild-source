package org.cmdbuild.service.rest.dto;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.PARENT;
import static org.cmdbuild.service.rest.constants.Serialization.PROTOTYPE;
import static org.cmdbuild.service.rest.constants.Serialization.SIMPLE_CLASS_DETAIL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = SIMPLE_CLASS_DETAIL)
public class SimpleProcessDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SimpleProcessDetail> {

		private String name;
		private String description;
		private String parent;
		private Boolean prototype;

		private Builder() {
			// use static method
		}

		@Override
		public SimpleProcessDetail build() {
			validate();
			return new SimpleProcessDetail(this);
		}

		private void validate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

		public Builder thatIsPrototype(final Boolean prototype) {
			this.prototype = prototype;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String name;
	private String description;
	private String parent;
	private boolean prototype;

	SimpleProcessDetail() {
		// package visibility
	}

	private SimpleProcessDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.parent = builder.parent;
		this.prototype = builder.prototype;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = PARENT)
	public String getParent() {
		return parent;
	}

	void setParent(final String parent) {
		this.parent = parent;
	}

	@XmlAttribute(name = PROTOTYPE)
	public boolean isPrototype() {
		return prototype;
	}

	void setPrototype(final boolean prototype) {
		this.prototype = prototype;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SimpleProcessDetail)) {
			return false;
		}

		final SimpleProcessDetail other = SimpleProcessDetail.class.cast(obj);

		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.parent, other.parent) //
				.append(this.prototype, other.prototype) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.name) //
				.append(this.description) //
				.append(this.parent) //
				.append(this.prototype) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
