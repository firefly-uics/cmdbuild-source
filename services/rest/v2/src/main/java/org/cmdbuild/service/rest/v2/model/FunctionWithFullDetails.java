package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.METADATA;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class FunctionWithFullDetails extends FunctionWithBasicDetails {

	private Map<String, Object> metadata;

	FunctionWithFullDetails() {
		// package visibility
	}

	@XmlElement(name = METADATA, nillable = true)
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

		if (!(obj instanceof FunctionWithFullDetails)) {
			return false;
		}

		final FunctionWithFullDetails other = FunctionWithFullDetails.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.append(this.metadata, other.metadata) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(metadata) //
				.toHashCode();
	}

}
