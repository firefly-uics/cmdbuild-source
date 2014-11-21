package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Models.newAttachmentMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.ProcessInstanceAttachmentMetadata;
import org.cmdbuild.service.rest.model.AttachmentMetadata;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class CxfProcessInstanceAttachmentMetadata implements ProcessInstanceAttachmentMetadata {

	private static final Iterable<MetadataGroup> NO_METADATA_GROUPS = emptyList();

	private static final Function<StoredDocument, AttachmentMetadata> TO_ATTACHMENT = new Function<StoredDocument, AttachmentMetadata>() {

		@Override
		public AttachmentMetadata apply(final StoredDocument input) {
			return newAttachmentMetadata() //
					.withId(input.getName()) //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.withVersion(input.getVersion()) //
					.withAuthor(input.getAuthor()) //
					.withCreated(input.getCreated()) //
					.withModified(input.getModified()) //
					.withCategory(input.getCategory()) //
					.withExtra(metadata(input.getMetadataGroups())) //
					.build();
		}

		private Map<String, Object> metadata(final Iterable<MetadataGroup> metadataGroups) {
			final Map<String, Object> metadata = newHashMap();
			for (final MetadataGroup element : defaultIfNull(metadataGroups, NO_METADATA_GROUPS)) {
				for (final Metadata subElement : element.getMetadata()) {
					metadata.put(subElement.getName(), subElement.getValue());
				}
			}
			return metadata;
		}

	};

	private final DmsLogic dmsLogic;
	private final WorkflowLogic workflowLogic;
	private final ErrorHandler errorHandler;

	public CxfProcessInstanceAttachmentMetadata(final ErrorHandler errorHandler, final DmsLogic dmsLogic,
			final WorkflowLogic workflowLogic) {
		this.errorHandler = errorHandler;
		this.dmsLogic = dmsLogic;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseSingle<AttachmentMetadata> read(final String classId, final Long cardId, final String attachmentId) {
		assureProcessAndInstance(classId, cardId, attachmentId);
		final Optional<StoredDocument> document = dmsLogic.search(classId, cardId, attachmentId);
		if (!document.isPresent()) {
			errorHandler.attachmentNotFound(attachmentId);
		}
		final AttachmentMetadata element = TO_ATTACHMENT.apply(document.get());
		return newResponseSingle(AttachmentMetadata.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId,
			final AttachmentMetadata attachmentMetadata) {
		assureProcessAndInstance(classId, cardId, attachmentId);
		if (attachmentMetadata == null) {
			errorHandler.missingAttachmentMetadata();
		}
		dmsLogic.updateDescriptionAndMetadata(classId, cardId, attachmentId, attachmentMetadata.getCategory(),
				attachmentMetadata.getDescription(), metadataGroupsOf(attachmentMetadata));
	}

	private void assureProcessAndInstance(final String classId, final Long cardId, final String attachmentId) {
		final CMClass targetClass = workflowLogic.findProcessClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			workflowLogic.getProcessInstance(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
	}

	private Collection<MetadataGroup> metadataGroupsOf(final AttachmentMetadata attachment) {
		final Collection<MetadataGroup> metadataGroups = newArrayList();
		for (final MetadataGroupDefinition groupDefinition : dmsLogic.getCategoryDefinition(attachment.getCategory())
				.getMetadataGroupDefinitions()) {
			final Map<String, Object> attachmentMetadata = attachment.getExtra();
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

}
