package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.model.Models.newAttachment;

import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.cxf.serialization.ToAttachment;
import org.cmdbuild.service.rest.model.Attachment;

import com.google.common.base.Function;
import com.google.common.base.Optional;

abstract class AttachmentsManagement {

	private static final Function<StoredDocument, Attachment> TO_ATTACHMENT_WITH_NO_METADATA = ToAttachment
			.newInstance() //
			.build();

	private static final Function<StoredDocument, Attachment> TO_ATTACHMENT_WITH_METADATA = ToAttachment.newInstance() //
			.withMetadata(true) //
			.build();

	private static final Attachment NULL_ATTACHMENT = newAttachment().build();

	private final DmsLogic dmsLogic;
	private final UserStore userStore;

	public AttachmentsManagement(final DmsLogic dmsLogic, final UserStore userStore) {
		this.dmsLogic = dmsLogic;
		this.userStore = userStore;
	}

	protected void store(final String classId, final Long cardId, final String attachmentId,
			final Attachment attachment, final DataHandler dataHandler) throws Exception {
		if (dataHandler != null) {
			final Attachment _attachment = defaultIfNull(attachment, NULL_ATTACHMENT);
			final String author = userStore.getUser().getAuthenticatedUser().getUsername();
			dmsLogic.upload( //
					author, //
					classId, //
					cardId, //
					dataHandler.getInputStream(), //
					attachmentId, //
					_attachment.getCategory(), //
					_attachment.getDescription(), //
					metadataGroupsOf(_attachment) //
			);
		} else if (attachment != null) {
			dmsLogic.updateDescriptionAndMetadata(classId, //
					cardId, //
					attachmentId, //
					attachment.getCategory(), //
					attachment.getDescription(), //
					metadataGroupsOf(attachment) //
			);
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
				return (rawValue == null) ? EMPTY : rawValue.toString();
			}

		};
	}

	protected Iterable<Attachment> search(final String classId, final Long cardId) {
		final Iterable<StoredDocument> documents = dmsLogic.search(classId, cardId);
		final Iterable<Attachment> elements = from(documents) //
				.transform(TO_ATTACHMENT_WITH_NO_METADATA);
		return elements;
	}

	protected Optional<Attachment> search(final String classId, final Long cardId, final String attachmentId) {
		final Optional<StoredDocument> document = dmsLogic.search(classId, cardId, attachmentId);
		if (!document.isPresent()) {
			return Optional.absent();
		}
		final Attachment element = TO_ATTACHMENT_WITH_METADATA.apply(document.get());
		return Optional.of(element);
	}

	protected DataHandler download(final String classId, final Long cardId, final String attachmentId) {
		return dmsLogic.download(classId, cardId, attachmentId);
	}

	protected void delete(final String classId, final Long cardId, final String attachmentId) {
		dmsLogic.delete(classId, cardId, attachmentId);
	}

}