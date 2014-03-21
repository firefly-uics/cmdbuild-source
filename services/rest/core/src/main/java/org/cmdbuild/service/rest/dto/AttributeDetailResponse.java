package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.ATTRIBUTE_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.dto.Constants.DATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = ATTRIBUTE_DETAIL_RESPONSE)
public class AttributeDetailResponse extends ListResponse<AttributeDetail> {

	public static Builder<AttributeDetail, AttributeDetailResponse> newInstance() {
		return new Builder<AttributeDetail, AttributeDetailResponse>() {

			@Override
			protected AttributeDetailResponse doBuild() {
				return new AttributeDetailResponse(this);
			}

		};
	}

	AttributeDetailResponse() {
		// package visibility
	}

	private AttributeDetailResponse(final Builder<AttributeDetail, AttributeDetailResponse> builder) {
		super(builder);
	}

	@XmlElement(name = DATA, type = AttributeDetail.class)
	@JsonProperty(DATA)
	public Collection<AttributeDetail> getElements() {
		return super.getElements();
	}

}
