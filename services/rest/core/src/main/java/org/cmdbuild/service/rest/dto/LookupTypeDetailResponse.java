package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DATA;
import static org.cmdbuild.service.rest.dto.Constants.LOOKUP_TYPE_DETAIL_RESPONSE;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL_RESPONSE)
public class LookupTypeDetailResponse extends ListResponse<LookupTypeDetail> {

	public static Builder<LookupTypeDetail, LookupTypeDetailResponse> newInstance() {
		return new Builder<LookupTypeDetail, LookupTypeDetailResponse>() {

			@Override
			protected LookupTypeDetailResponse doBuild() {
				return new LookupTypeDetailResponse(this);
			}

		};
	}

	LookupTypeDetailResponse() {
		// package visibility
	}

	private LookupTypeDetailResponse(final Builder<LookupTypeDetail, LookupTypeDetailResponse> builder) {
		super(builder);
	}

	@XmlElement(name = DATA, type = LookupTypeDetail.class)
	@JsonProperty(DATA)
	public Collection<LookupTypeDetail> getElements() {
		return super.getElements();
	}

}
