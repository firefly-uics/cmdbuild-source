package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.ATTRIBUTE_DETAIL_RESPONSE;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = ATTRIBUTE_DETAIL_RESPONSE)
public class CardResponse extends SimpleResponse<Map<String, Object>> {

	public static Builder<Map<String, Object>, CardResponse> newInstance() {
		return new Builder<Map<String, Object>, CardResponse>() {

			@Override
			protected CardResponse doBuild() {
				return new CardResponse(this);
			}

		};
	}

	CardResponse() {
		// package visibility
	}

	private CardResponse(final Builder<Map<String, Object>, CardResponse> builder) {
		super(builder);
	}

	@Override
	public Map<String, Object> getElement() {
		return super.getElement();
	}

}
