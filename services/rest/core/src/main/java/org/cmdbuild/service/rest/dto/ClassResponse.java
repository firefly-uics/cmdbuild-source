package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DATA;
import static org.cmdbuild.service.rest.dto.Constants.LOOKUP_TYPE_RESPONSE;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_RESPONSE)
public class ClassResponse extends SimpleResponse<FullClassDetail> {

	public static Builder<FullClassDetail, ClassResponse> newInstance() {
		return new Builder<FullClassDetail, ClassResponse>() {

			@Override
			protected ClassResponse doBuild() {
				return new ClassResponse(this);
			}

		};
	}

	ClassResponse() {
		// package visibility
	}

	private ClassResponse(final Builder<FullClassDetail, ClassResponse> builder) {
		super(builder);
	}

	@Override
	@XmlElement(name = DATA, type = FullClassDetail.class)
	@JsonProperty(DATA)
	public FullClassDetail getElement() {
		return super.getElement();
	}

}
