package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.CLASS_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.constants.Serialization.DATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = CLASS_DETAIL_RESPONSE)
public class ClassListResponse extends ListResponse<SimpleClassDetail> {

	public static Builder<SimpleClassDetail, ClassListResponse> newInstance() {
		return new Builder<SimpleClassDetail, ClassListResponse>() {

			@Override
			protected ClassListResponse doBuild() {
				return new ClassListResponse(this);
			}

		};
	}

	ClassListResponse() {
		// package visibility
	}

	private ClassListResponse(final Builder<SimpleClassDetail, ClassListResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = SimpleClassDetail.class)
	@JsonProperty(DATA)
	public Collection<SimpleClassDetail> getElements() {
		return super.getElements();
	}

}
