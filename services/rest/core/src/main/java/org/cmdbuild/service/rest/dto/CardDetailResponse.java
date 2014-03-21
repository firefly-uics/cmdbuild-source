package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.CARD_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.dto.Constants.DATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = CARD_DETAIL_RESPONSE)
public class CardDetailResponse extends ListResponse<CardDetail> {

	public static Builder<CardDetail, CardDetailResponse> newInstance() {
		return new Builder<CardDetail, CardDetailResponse>() {

			@Override
			protected CardDetailResponse doBuild() {
				return new CardDetailResponse(this);
			}

		};
	}

	CardDetailResponse() {
		// package visibility
	}

	private CardDetailResponse(final Builder<CardDetail, CardDetailResponse> builder) {
		super(builder);
	}

	@XmlElement(name = DATA, type = CardDetail.class)
	@JsonProperty(DATA)
	public Collection<CardDetail> getElements() {
		return super.getElements();
	}

}
