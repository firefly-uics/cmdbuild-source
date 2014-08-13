package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DATA;
import static org.cmdbuild.service.rest.dto.Constants.LOOKUP_TYPE_LIST_RESPONSE;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_LIST_RESPONSE)
public class LookupDetailResponse extends ListResponse<LookupDetail> {

	public static Builder<LookupDetail, LookupDetailResponse> newInstance() {
		return new Builder<LookupDetail, LookupDetailResponse>() {

			@Override
			protected LookupDetailResponse doBuild() {
				return new LookupDetailResponse(this);
			}

		};
	}

	LookupDetailResponse() {
		// package visibility
	}

	private LookupDetailResponse(final Builder<LookupDetail, LookupDetailResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = LookupDetail.class)
	@JsonProperty(DATA)
	public Collection<LookupDetail> getElements() {
		return super.getElements();
	}

}
