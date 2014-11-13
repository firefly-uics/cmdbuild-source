package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.service.rest.model.Builders.newAttachment;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;

import java.util.Map;

import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.CardAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;

import com.google.common.base.Function;

public class CxfCardAttachments implements CardAttachments {

	private final DmsLogic dmsLogic;

	public CxfCardAttachments(final DmsLogic dmsLogic) {
		this.dmsLogic = dmsLogic;
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		final Iterable<StoredDocument> documents = dmsLogic.search(classId, cardId);
		final Iterable<Attachment> elements = from(documents) //
				.transform(new Function<StoredDocument, Attachment>() {

					@Override
					public Attachment apply(final StoredDocument input) {
						return newAttachment() //
								.withId(input.getName()) //
								.withDescription(input.getDescription()) //
								.withVersion(input.getVersion()) //
								.withAuthor(input.getAuthor()) //
								.withCreated(input.getCreated()) //
								.withModified(input.getModified()) //
								.withCategory(input.getCategory()) //
								.withMetadata(metadata(input.getMetadataGroups())) //
								.build();
					}

					private Map<String, Object> metadata(final Iterable<MetadataGroup> metadataGroups) {
						final Map<String, Object> metadata = newHashMap();
						for (final MetadataGroup element : metadataGroups) {
							for (final Metadata _metadata : element.getMetadata()) {
								metadata.put(_metadata.getName(), _metadata.getValue());
							}
						}
						return metadata;
					}

				});
		return newResponseMultiple(Attachment.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

}
