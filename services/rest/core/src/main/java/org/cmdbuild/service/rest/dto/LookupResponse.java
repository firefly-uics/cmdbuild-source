package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_RESPONSE;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_RESPONSE)
public class LookupResponse extends SimpleResponse<LookupDetail> {

	public static Builder<LookupDetail, LookupResponse> newInstance() {
		return new Builder<LookupDetail, LookupResponse>() {

			@Override
			protected LookupResponse doBuild() {
				return new LookupResponse(this);
			}

		};
	}

	LookupResponse() {
		// package visibility
	}

	private LookupResponse(final Builder<LookupDetail, LookupResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = LookupDetail.class)
	@JsonProperty(DATA)
	public LookupDetail getElement() {
		return super.getElement();
	}

}
