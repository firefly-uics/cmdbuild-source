package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.model.Builders.newAttachmentCategory;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.service.rest.model.AttachmentCategory;

import com.google.common.base.Function;

public class ToAttachmentCategory implements Function<DocumentTypeDefinition, AttachmentCategory> {

	@Override
	public AttachmentCategory apply(final DocumentTypeDefinition input) {
		return newAttachmentCategory() //
				.withId(input.getName()) //
				.withDescription(input.getName()) //
				.build();
	}

}