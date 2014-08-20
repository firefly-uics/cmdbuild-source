package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.CLASS_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.constants.Serialization.DATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = CLASS_DETAIL_RESPONSE)
public class DomainListResponse extends ListResponse<SimpleDomainDetail> {

	public static Builder<SimpleDomainDetail, DomainListResponse> newInstance() {
		return new Builder<SimpleDomainDetail, DomainListResponse>() {

			@Override
			protected DomainListResponse doBuild() {
				return new DomainListResponse(this);
			}

		};
	}

	DomainListResponse() {
		// package visibility
	}

	private DomainListResponse(final Builder<SimpleDomainDetail, DomainListResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = SimpleDomainDetail.class)
	@JsonProperty(DATA)
	public Collection<SimpleDomainDetail> getElements() {
		return super.getElements();
	}

}
