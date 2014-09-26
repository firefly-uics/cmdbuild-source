package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.WRITABLE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = PROCESS_ACTIVITY)
public class ProcessActivityWithBasicDetails extends AbstractModelWithId {

	private boolean writable;

	ProcessActivityWithBasicDetails() {
		// package visibility
	}

	@XmlAttribute(name = WRITABLE)
	public boolean isWritable() {
		return writable;
	}

	void setWritable(final boolean writable) {
		this.writable = writable;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessActivityWithBasicDetails)) {
			return false;
		}

		final ProcessActivityWithBasicDetails other = ProcessActivityWithBasicDetails.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.writable, other.writable) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(writable) //
				.toHashCode();
	}

}
