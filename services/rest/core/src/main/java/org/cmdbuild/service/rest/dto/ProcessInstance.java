package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_INSTANCE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = PROCESS_INSTANCE)
public class ProcessInstance {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ProcessInstance> {

		private String name;

		private Builder() {
			// use static method
		}

		@Override
		public ProcessInstance build() {
			validate();
			return new ProcessInstance(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	ProcessInstance() {
		// package visibility
	}

	private String name;

	private ProcessInstance(final Builder builder) {
		this.name = builder.name;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessInstance)) {
			return false;
		}

		final ProcessInstance other = ProcessInstance.class.cast(obj);
		return new EqualsBuilder().append(this.name, other.name) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
