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
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.model.Attachment;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Optional;

public class CxfCardAttachments extends AttachmentsManagement implements AllInOneCardAttachments {

	private final DataAccessLogic dataAccessLogic;
	private final ErrorHandler errorHandler;

	public CxfCardAttachments(final ErrorHandler errorHandler, final DmsLogic dmsLogic,
			final DataAccessLogic dataAccessLogic, final UserStore userStore) {
		super(dmsLogic, userStore);
		this.errorHandler = errorHandler;
		this.dataAccessLogic = dataAccessLogic;
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
		try {
			store(classId, cardId, dataHandler.getName(), attachment, dataHandler);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return newResponseSingle(String.class) //
				.withElement(dataHandler.getName()) //
				.build();
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		assureClassAndCard(classId, cardId);
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
		assureClassAndCard(classId, cardId, attachmentId);
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
		assureClassAndCard(classId, cardId);
		return super.download(classId, cardId, attachmentId);
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId, final Attachment attachment,
			final DataHandler dataHandler) {
		assureClassAndCard(classId, cardId);
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
		try {
			store(classId, cardId, attachmentId, attachment, dataHandler);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		assureClassAndCard(classId, cardId);
		super.delete(classId, cardId, attachmentId);
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

}
