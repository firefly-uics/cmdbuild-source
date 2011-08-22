package org.cmdbuild.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.documents.DefaultDocumentFactory;
import org.cmdbuild.dms.documents.DocumentDelete;
import org.cmdbuild.dms.documents.DocumentDownload;
import org.cmdbuild.dms.documents.DocumentFactory;
import org.cmdbuild.dms.documents.DocumentSearch;
import org.cmdbuild.dms.documents.DocumentUpdate;
import org.cmdbuild.dms.documents.StorableDocument;
import org.cmdbuild.dms.documents.StoredDocument;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;

public class DmsLogic {

	private static Logger logger = Log.DMS;

	private final DmsService service;
	private UserContext userContext;

	public DmsLogic(final DmsService service) {
		this.service = service;
		service.setProperties(DmsProperties.getInstance());
	}

	public UserContext getUserContext() {
		return userContext;
	}

	public void setUserContext(final UserContext userContext) {
		this.userContext = userContext;
	}

	private DocumentFactory createDocumentFactory(final String className) {
		final Collection<String> path = userContext.tables().tree().path(className);
		return new DefaultDocumentFactory(path);
	}

	private void assureWritePrivilege(final String className) {
		final ITable schema = userContext.tables().get(className);
		userContext.privileges().assureWritePrivilege(schema);
	}

	public List<StoredDocument> search(final String className, final int cardId) {
		final DocumentSearch document = createDocumentFactory(className) //
				.createDocumentSearch(className, cardId);
		return service.search(document);
	}

	public void upload(final String author, final String className, final int cardId, final InputStream inputStream,
			final String fileName, final String category, final String description) throws IOException, CMDBException {
		final StorableDocument document = createDocumentFactory(className) //
				.createStorableDocument(author, className, cardId, inputStream, fileName, category, description);
		assureWritePrivilege(className);
		try {
			service.upload(document);
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' to card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_UPLOAD_FAILED.createException();
		}
	}

	public DataHandler download(final String className, final int cardId, final String fileName) {
		final DocumentDownload document = createDocumentFactory(className) //
				.createDocumentDownload(className, cardId, fileName);
		try {
			final DataHandler dataHandler = service.download(document);
			return dataHandler;
		} catch (final Exception e) {
			final String message = String.format("error downloading file '%s' for card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw NotFoundExceptionType.ATTACHMENT_NOTFOUND
					.createException(fileName, className, String.valueOf(cardId));
		}
	}

	public void delete(final String className, final int cardId, final String fileName) throws NotFoundException {
		final DocumentDelete document = createDocumentFactory(className) //
				.createDocumentDelete(className, cardId, fileName);
		assureWritePrivilege(className);
		try {
			service.delete(document);
		} catch (final Exception e) {
			final String message = String.format("error deleting file '%s' for card '%s' with id '%d'", //
					fileName, className, cardId);
			logger.error(message, e);
			throw ORMException.ORMExceptionType.ORM_ATTACHMENT_DELETE_FAILED.createException();
		}
	}

	public void updateDescription(final String className, final int cardId, final String filename,
			final String description) {
		final DocumentUpdate document = createDocumentFactory(className) //
				.createDocumentUpdate(className, cardId, filename, description);
		assureWritePrivilege(className);
		try {
			service.updateDescription(document);
		} catch (final Exception e) {
			final String message = String.format("error updating description to file '%s' for card '%s' with id '%d'", //
					filename, className, cardId);
			logger.error(message, e);
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
	}

}
