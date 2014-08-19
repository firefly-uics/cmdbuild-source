package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DATA;
import static org.cmdbuild.service.rest.constants.Serialization.MENU_DETAIL_RESPONSE;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = MENU_DETAIL_RESPONSE)
public class MenuDetailResponse extends SimpleResponse<MenuDetail> {

	public static Builder<MenuDetail, MenuDetailResponse> newInstance() {
		return new Builder<MenuDetail, MenuDetailResponse>() {

			@Override
			protected MenuDetailResponse doBuild() {
				return new MenuDetailResponse(this);
			}

		};
	}

	MenuDetailResponse() {
		// package visibility
	}

	private MenuDetailResponse(final Builder<MenuDetail, MenuDetailResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = MenuDetail.class)
	@JsonProperty(DATA)
	public MenuDetail getElement() {
		return super.getElement();
	}

}
