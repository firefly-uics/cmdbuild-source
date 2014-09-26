package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.RESPONSE_METADATA;
import static org.cmdbuild.service.rest.constants.Serialization.TOTAL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = RESPONSE_METADATA)
public class DetailResponseMetadata extends AbstractModel {

	private Long total;

	DetailResponseMetadata() {
		// package visibility
	}

	@XmlAttribute(name = TOTAL)
	public Long getTotal() {
		return total;
	}

	void setTotal(final Long total) {
		this.total = total;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DetailResponseMetadata)) {
			return false;
		}
		final DetailResponseMetadata other = DetailResponseMetadata.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.total, other.total) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.total) //
				.toHashCode();
	}

}
