package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.ATTRIBUTE_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.dto.Constants.DATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = ATTRIBUTE_DETAIL_RESPONSE)
public class AttributeValueDetailResponse extends ListResponse<AttributeValueDetail> {

	public static Builder<AttributeValueDetail, AttributeValueDetailResponse> newInstance() {
		return new Builder<AttributeValueDetail, AttributeValueDetailResponse>() {

			@Override
			protected AttributeValueDetailResponse doBuild() {
				return new AttributeValueDetailResponse(this);
			}

		};
	}

	AttributeValueDetailResponse() {
		// package visibility
	}

	private AttributeValueDetailResponse(final Builder<AttributeValueDetail, AttributeValueDetailResponse> builder) {
		super(builder);
	}

	@XmlElement(name = DATA, type = AttributeValueDetail.class)
	@JsonProperty(DATA)
	public Collection<AttributeValueDetail> getElements() {
		return super.getElements();
	}

}
