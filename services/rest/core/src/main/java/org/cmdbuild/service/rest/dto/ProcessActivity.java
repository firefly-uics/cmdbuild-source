package org.cmdbuild.service.rest.dto;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.WRITABLE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = PROCESS_ACTIVITY)
public class ProcessActivity {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ProcessActivity> {

		private String id;
		private Boolean writable;

		private Builder() {
			// use static method
		}

		@Override
		public ProcessActivity build() {
			validate();
			return new ProcessActivity(this);
		}

		private void validate() {
			writable = defaultIfNull(writable, FALSE);
		}

		public Builder withId(final String id) {
			this.id = id;
			return this;
		}

		public Builder withWritableStatus(final boolean writable) {
			this.writable = writable;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	ProcessActivity() {
		// package visibility
	}

	private String id;
	private boolean writable;

	private ProcessActivity(final Builder builder) {
		this.id = builder.id;
		this.writable = builder.writable;
	}

	@XmlAttribute(name = ID)
	public String getId() {
		return id;
	}

	void setId(final String id) {
		this.id = id;
	}

	@XmlAttribute(name = WRITABLE)
	public boolean isWritable() {
		return writable;
	}

	public void setWritable(final boolean writable) {
		this.writable = writable;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessActivity)) {
			return false;
		}

		final ProcessActivity other = ProcessActivity.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.writable, other.writable) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(writable) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
