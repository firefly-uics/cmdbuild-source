package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.CARD_DETAIL_RESPONSE;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = CARD_DETAIL_RESPONSE)
public class CardListResponse extends ListResponse<Map<String, Object>> {

	public static Builder<Map<String, Object>, CardListResponse> newInstance() {
		return new Builder<Map<String, Object>, CardListResponse>() {

			@Override
			protected CardListResponse doBuild() {
				return new CardListResponse(this);
			}

		};
	}

	CardListResponse() {
		// package visibility
	}

	private CardListResponse(final Builder<Map<String, Object>, CardListResponse> builder) {
		super(builder);
	}

}
