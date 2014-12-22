package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Optional;

public class CxfProcessInstanceAttachments extends AttachmentsManagement implements AllInOneProcessInstanceAttachments {

	private final WorkflowLogic workflowLogic;
	private final ErrorHandler errorHandler;

	public CxfProcessInstanceAttachments(final ErrorHandler errorHandler, final DmsLogic dmsLogic,
			final WorkflowLogic workflowLogic, final UserStore userStore) {
		super(dmsLogic, userStore);
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public ResponseSingle<String> create(final String processId, final Long instanceId, final Attachment attachment,
			final DataHandler dataHandler) {
		assureProcessAndInstance(processId, instanceId);
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		if (isBlank(dataHandler.getName())) {
			errorHandler.missingAttachmentName();
		}
		try {
			store(processId, instanceId, dataHandler.getName(), attachment, dataHandler);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return newResponseSingle(String.class) //
				.withElement(dataHandler.getName()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		assureProcessAndInstance(classId, cardId);
		final Iterable<Attachment> elements = search(classId, cardId);
		return newResponseMultiple(Attachment.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Attachment> read(final String classId, final Long cardId, final String attachmentId) {
		assureProcessAndInstance(classId, cardId, attachmentId);
		final Optional<Attachment> element = search(classId, cardId, attachmentId);
		if (!element.isPresent()) {
			errorHandler.attachmentNotFound(attachmentId);
		}
		return newResponseSingle(Attachment.class) //
				.withElement(element.get()) //
				.build();
	}

	@Override
	public DataHandler download(final String classId, final Long cardId, final String attachmentId) {
		assureProcessAndInstance(classId, cardId);
		return super.download(classId, cardId, attachmentId);
	}

	@Override
	public void update(final String processId, final Long instanceId, final String attachmentId,
			final Attachment attachment, final DataHandler dataHandler) {
		assureProcessAndInstance(processId, instanceId);
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
		try {
			store(processId, instanceId, attachmentId, attachment, dataHandler);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		assureProcessAndInstance(classId, cardId);
		super.delete(classId, cardId, attachmentId);
	}

	private void assureProcessAndInstance(final String classId, final Long cardId) {
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

}
