package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.*;
import static org.cmdbuild.service.rest.dto.Constants.TOTAL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = RESPONSE_METADATA)
public class DetailResponseMetadata {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DetailResponseMetadata> {

		private int total;

		private Builder() {
			// use factory method
		}

		@Override
		public DetailResponseMetadata build() {
			validate();
			return new DetailResponseMetadata(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withTotal(final int total) {
			this.total = total;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private int total;

	DetailResponseMetadata() {
		// package visibility
	}

	public DetailResponseMetadata(final Builder builder) {
		this.total = builder.total;
	}

	@XmlAttribute(name = TOTAL)
	public int getTotal() {
		return total;
	}

	void setTotal(final int total) {
		this.total = total;
	}

}
