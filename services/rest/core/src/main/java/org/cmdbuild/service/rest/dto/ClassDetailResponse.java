package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.CLASS_DETAIL_RESPONSE;
import static org.cmdbuild.service.rest.dto.Constants.DATA;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = CLASS_DETAIL_RESPONSE)
public class ClassDetailResponse extends ListResponse<ClassDetail> {

	public static Builder<ClassDetail, ClassDetailResponse> newInstance() {
		return new Builder<ClassDetail, ClassDetailResponse>() {

			@Override
			protected ClassDetailResponse doBuild() {
				return new ClassDetailResponse(this);
			}

		};
	}

	ClassDetailResponse() {
		// package visibility
	}

	private ClassDetailResponse(final Builder<ClassDetail, ClassDetailResponse> builder) {
		super(builder);
	}

	@XmlElement(name = DATA, type = ClassDetail.class)
	@JsonProperty(DATA)
	public Collection<ClassDetail> getElements() {
		return super.getElements();
	}

}
