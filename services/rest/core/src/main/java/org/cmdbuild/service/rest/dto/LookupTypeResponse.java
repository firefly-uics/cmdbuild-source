package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_RESPONSE;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_RESPONSE)
public class LookupTypeResponse extends SimpleResponse<LookupTypeDetail> {

	public static Builder<LookupTypeDetail, LookupTypeResponse> newInstance() {
		return new Builder<LookupTypeDetail, LookupTypeResponse>() {

			@Override
			protected LookupTypeResponse doBuild() {
				return new LookupTypeResponse(this);
			}

		};
	}

	LookupTypeResponse() {
		// package visibility
	}

	private LookupTypeResponse(final Builder<LookupTypeDetail, LookupTypeResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = LookupTypeDetail.class)
	@JsonProperty(DATA)
	public LookupTypeDetail getElement() {
		return super.getElement();
	}

}
