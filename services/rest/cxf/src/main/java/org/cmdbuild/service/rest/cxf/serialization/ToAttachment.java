package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.model.Models.newAttachment;

import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.service.rest.model.Attachment;

import com.google.common.base.Function;

public class ToAttachment implements Function<StoredDocument, Attachment> {

	@Override
	public Attachment apply(final StoredDocument input) {
		return newAttachment() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withCategory(input.getCategory()) //
				.withDescription(input.getDescription()) //
				.withVersion(input.getVersion()) //
				.withAuthor(input.getAuthor()) //
				.withCreated(input.getCreated()) //
				.withModified(input.getModified()) //
				.build();
	}

}
