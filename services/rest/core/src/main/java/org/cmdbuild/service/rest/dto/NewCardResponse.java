package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.NEW_CARD_RESPONSE;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = NEW_CARD_RESPONSE)
public class NewCardResponse extends SimpleResponse<Long> {

	public static Builder<Long, NewCardResponse> newInstance() {
		return new Builder<Long, NewCardResponse>() {

			@Override
			protected NewCardResponse doBuild() {
				return new NewCardResponse(this);
			}

		};
	}

	NewCardResponse() {
		// package visibility
	}

	private NewCardResponse(final Builder<Long, NewCardResponse> builder) {
		super(builder);
	}

}
