package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Models.newAttachment;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.cxf.serialization.ToAttachment;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class CxfCardAttachments implements AllInOneCardAttachments {

	private static final Function<StoredDocument, Attachment> TO_ATTACHMENT_WITH_NO_METADATA = ToAttachment
			.newInstance() //
			.build();
	private static final Function<StoredDocument, Attachment> TO_ATTACHMENT_WITH_METADATA = ToAttachment.newInstance() //
			.withMetadata(true) //
			.build();

	private static final Attachment NULL_ATTACHMENT = newAttachment().build();

	private final DmsLogic dmsLogic;
	private final DataAccessLogic dataAccessLogic;
	private final ErrorHandler errorHandler;
	private final UserStore userStore;

	public CxfCardAttachments(final ErrorHandler errorHandler, final DmsLogic dmsLogic,
			final DataAccessLogic dataAccessLogic, final UserStore userStore) {
		this.errorHandler = errorHandler;
		this.dmsLogic = dmsLogic;
		this.dataAccessLogic = dataAccessLogic;
		this.userStore = userStore;
	}

	@Override
	public ResponseSingle<String> create(final String classId, final Long cardId, final Attachment attachment,
			final DataHandler dataHandler) {
		assureClassAndCard(classId, cardId);
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		if (isBlank(dataHandler.getName())) {
			errorHandler.missingAttachmentName();
		}
		upload(classId, cardId, dataHandler.getName(), defaultIfNull(attachment, NULL_ATTACHMENT), dataHandler);
		return newResponseSingle(String.class) //
				.withElement(dataHandler.getName()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		assureClassAndCard(classId, cardId);
		final Iterable<StoredDocument> documents = dmsLogic.search(classId, cardId);
		final Iterable<Attachment> elements = from(documents) //
				.transform(TO_ATTACHMENT_WITH_NO_METADATA);
		return newResponseMultiple(Attachment.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Attachment> read(final String classId, final Long cardId, final String attachmentId) {
		assureClassAndCard(classId, cardId, attachmentId);
		final Optional<StoredDocument> document = dmsLogic.search(classId, cardId, attachmentId);
		if (!document.isPresent()) {
			errorHandler.attachmentNotFound(attachmentId);
		}
		final Attachment element = TO_ATTACHMENT_WITH_METADATA.apply(document.get());
		return newResponseSingle(Attachment.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public DataHandler download(final String classId, final Long cardId, final String attachmentId) {
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
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		upload(classId, cardId, attachmentId, attachment, dataHandler);
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

	private void assureClassAndCard(final String classId, final Long cardId, final String attachmentId) {
		final CMClass targetClass = dataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			dataAccessLogic.fetchCard(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
	}

	private void upload(final String classId, final Long cardId, final String attachmentId,
			final Attachment attachment, final DataHandler dataHandler) {
		try {
			final String author = userStore.getUser().getAuthenticatedUser().getUsername();
			dmsLogic.upload( //
					author, //
					classId, //
					cardId, //
					dataHandler.getInputStream(), //
					attachmentId, //
					attachment.getCategory(), //
					attachment.getDescription(), //
					metadataGroupsOf(attachment) //
			);
		} catch (final Exception e) {
			errorHandler.propagate(e);
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

}
