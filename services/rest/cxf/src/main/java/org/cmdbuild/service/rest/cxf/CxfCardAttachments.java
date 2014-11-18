package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Builders.newAttachment;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.CardAttachments;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Function;

public class CxfCardAttachments implements CardAttachments {

	private static final Function<StoredDocument, Attachment> TO_ATTACHMENT = new Function<StoredDocument, Attachment>() {

		@Override
		public Attachment apply(final StoredDocument input) {
			return newAttachment() //
					.withId(input.getName()) //
					.withName(input.getName()) //
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
				for (final Metadata subElement : element.getMetadata()) {
					metadata.put(subElement.getName(), subElement.getValue());
				}
			}
			return metadata;
		}

	};

	private final DmsLogic dmsLogic;
	private final DataAccessLogic dataAccessLogic;
	private final ErrorHandler errorHandler;

	public CxfCardAttachments(final ErrorHandler errorHandler, final DmsLogic dmsLogic,
			final DataAccessLogic dataAccessLogic) {
		this.errorHandler = errorHandler;
		this.dmsLogic = dmsLogic;
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ResponseSingle<String> create(final String classId, final Long cardId, final Attachment attachment,
			final DataHandler dataHandler) {
		assureClassAndCard(classId, cardId);
		if (attachment == null) {
			errorHandler.missingAttachment();
		} else if (isBlank(attachment.getName())) {
			errorHandler.missingAttachmentName();
		}
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		upload(classId, cardId, attachment.getName(), attachment, dataHandler, metadataGroupsOf(attachment));
		return newResponseSingle(String.class) //
				.withElement(attachment.getName()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		assureClassAndCard(classId, cardId);
		final Iterable<StoredDocument> documents = dmsLogic.search(classId, cardId);
		final Iterable<Attachment> elements = from(documents) //
				.transform(TO_ATTACHMENT);
		return newResponseMultiple(Attachment.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

	@Override
	public DataHandler read(final String classId, final Long cardId, final String attachmentId) {
		assureClassAndCard(classId, cardId);
		return dmsLogic.download(classId, cardId, attachmentId);
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId, final Attachment attachment,
			final DataHandler dataHandler) {
		assureClassAndCard(classId, cardId);
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
		if (attachment == null) {
			errorHandler.missingAttachment();
		} else if (isBlank(attachment.getName())) {
			errorHandler.missingAttachmentName();
		}
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		upload(classId, cardId, attachmentId, attachment, dataHandler, metadataGroupsOf(attachment));
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		assureClassAndCard(classId, cardId);
		dmsLogic.delete(classId, cardId, attachmentId);
	}

	private void assureClassAndCard(final String classId, final Long cardId) {
		final CMClass targetClass = dataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			dataAccessLogic.fetchCard(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
	}

	private Collection<MetadataGroup> metadataGroupsOf(final Attachment attachment) {
		final Collection<MetadataGroup> metadataGroups = newArrayList();
		for (final MetadataGroupDefinition groupDefinition : dmsLogic.getCategoryDefinition(attachment.getCategory())
				.getMetadataGroupDefinitions()) {
			final Map<String, Object> attachmentMetadata = attachment.getMetadata();
			metadataGroups.add(metadataGroupOf(groupDefinition, attachmentMetadata));
		}
		return metadataGroups;
	}

	private MetadataGroup metadataGroupOf(final MetadataGroupDefinition groupDefinition,
			final Map<String, Object> attachmentMetadata) {
		return new MetadataGroup() {

			@Override
			public String getName() {
				return groupDefinition.getName();
			}

			@Override
			public Iterable<Metadata> getMetadata() {
				final Collection<Metadata> metadata = newArrayList();
				for (final MetadataDefinition metadataDefinition : groupDefinition.getMetadataDefinitions()) {
					final String name = metadataDefinition.getName();
					final Object rawValue = attachmentMetadata.get(name);
					if (attachmentMetadata.containsKey(name)) {
						metadata.add(metadataOf(name, rawValue));
					}
				}
				return metadata;
			}

		};
	}

	private Metadata metadataOf(final String name, final Object rawValue) {
		return new Metadata() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getValue() {
				return (rawValue == null) ? StringUtils.EMPTY : rawValue.toString();
			}

		};
	}

	private void upload(final String classId, final Long cardId, final String attachmentId,
			final Attachment attachment, final DataHandler dataHandler, final Collection<MetadataGroup> metadataGroups) {
		try {
			dmsLogic.upload(attachment.getAuthor(), classId, cardId, dataHandler.getInputStream(), attachmentId,
					attachment.getCategory(), attachment.getDescription(), metadataGroups);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

}
