package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Function;

public class CxfProcessInstanceAttachments implements AllInOneProcessInstanceAttachments {

	private static final Function<StoredDocument, String> TO_ATTACHMENT = new Function<StoredDocument, String>() {

		@Override
		public String apply(final StoredDocument input) {
			return input.getName();
		}

	};

	private static final String NO_DESCRIPTION = null;
	private static final String NO_CATEGORY = null;
	private static final Iterable<MetadataGroup> NO_METADATA_GROUPS = emptyList();

	private final DmsLogic dmsLogic;
	private final WorkflowLogic workflowLogic;
	private final ErrorHandler errorHandler;
	private final UserStore userStore;

	public CxfProcessInstanceAttachments(final ErrorHandler errorHandler, final DmsLogic dmsLogic,
			final WorkflowLogic workflowLogic, final UserStore userStore) {
		this.errorHandler = errorHandler;
		this.dmsLogic = dmsLogic;
		this.workflowLogic = workflowLogic;
		this.userStore = userStore;
	}

	@Override
	public ResponseSingle<String> create(final String classId, final Long cardId, final String attachmentName,
			final DataHandler dataHandler) {
		assureClassAndCard(classId, cardId);
		if (isBlank(attachmentName)) {
			errorHandler.missingAttachmentName();
		}
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		upload(classId, cardId, attachmentName, dataHandler);
		return newResponseSingle(String.class) //
				.withElement(attachmentName) //
				.build();
	}

	@Override
	public ResponseMultiple<String> read(final String classId, final Long cardId) {
		assureClassAndCard(classId, cardId);
		final Iterable<StoredDocument> documents = dmsLogic.search(classId, cardId);
		final Iterable<String> elements = from(documents) //
				.transform(TO_ATTACHMENT);
		return newResponseMultiple(String.class) //
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
	public void update(final String classId, final Long cardId, final String attachmentId, final DataHandler dataHandler) {
		assureClassAndCard(classId, cardId);
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		upload(classId, cardId, attachmentId, dataHandler);
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		assureClassAndCard(classId, cardId);
		dmsLogic.delete(classId, cardId, attachmentId);
	}

	private void assureClassAndCard(final String classId, final Long cardId) {
		final CMClass targetClass = workflowLogic.findProcessClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			workflowLogic.getProcessInstance(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
	}

	private void upload(final String classId, final Long cardId, final String attachmentId,
			final DataHandler dataHandler) {
		try {
			final String author = userStore.getUser().getAuthenticatedUser().getUsername();
			dmsLogic.upload(author, classId, cardId, dataHandler.getInputStream(), attachmentId, NO_CATEGORY,
					NO_DESCRIPTION, NO_METADATA_GROUPS);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

}
