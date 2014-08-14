package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_LIST_RESPONSE;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_LIST_RESPONSE)
public class LookupTypeListResponse extends ListResponse<LookupTypeDetail> {

	public static Builder<LookupTypeDetail, LookupTypeListResponse> newInstance() {
		return new Builder<LookupTypeDetail, LookupTypeListResponse>() {

			@Override
			protected LookupTypeListResponse doBuild() {
				return new LookupTypeListResponse(this);
			}

		};
	}

	LookupTypeListResponse() {
		// package visibility
	}

	private LookupTypeListResponse(final Builder<LookupTypeDetail, LookupTypeListResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = LookupTypeDetail.class)
	@JsonProperty(DATA)
	public Collection<LookupTypeDetail> getElements() {
		return super.getElements();
	}

}
