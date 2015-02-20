package org.cmdbuild.logic.email;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.ForwardingDmsService;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.SubjectHandler;

public class DefaultEmailAttachmentsLogic implements EmailAttachmentsLogic {

	private static class ConfigurationAwareDmsService extends ForwardingDmsService {

		private static final List<StoredDocument> EMPTY = Collections.emptyList();

		private final DmsService delegate;
		private final DmsConfiguration dmsConfiguration;

		public ConfigurationAwareDmsService(final DmsService delegate, final DmsConfiguration dmsConfiguration) {
			this.delegate = delegate;
			this.dmsConfiguration = dmsConfiguration;
		}

		@Override
		protected DmsService delegate() {
			return delegate;
		}

		@Override
		public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
			return dmsConfiguration.isEnabled() ? super.search(document) : EMPTY;
		}

	}

	// TODO do in a better way
	private static final boolean TEMPORARY = true;
	private static final boolean FINAL = false;

	private final CMDataView dataView;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final OperationUser operationUser;

	public DefaultEmailAttachmentsLogic( //
			final CMDataView dataView, //
			final EmailServiceFactory emailServiceFactory, //
			final Store<EmailAccount> emailAccountStore, //
			final SubjectHandler subjectHandler, //
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final Notifier notifier, //
			final OperationUser operationUser //
	) {
		this.dataView = dataView;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = new ConfigurationAwareDmsService(dmsService, dmsConfiguration);
		this.documentCreatorFactory = documentCreatorFactory;
		this.operationUser = operationUser;
	}

	@Override
	public void uploadAttachment( //
			final Upload upload //
	) throws IOException, CMDBException {
		InputStream inputStream = null;
		try {
			inputStream = upload.dataHandler.getInputStream();
			final String usableIdentifier = (upload.identifier == null) ? generateIdentifier() : upload.identifier;
			final StorableDocument document = documentCreator(upload.temporary) //
					.createStorableDocument( //
							operationUser.getAuthenticatedUser().getUsername(), //
							EMAIL_CLASS_NAME, //
							usableIdentifier, //
							inputStream, //
							upload.dataHandler.getName(), //
							dmsConfiguration.getLookupNameForAttachments(), //
							EMPTY);
			dmsService.upload(document);
		} catch (final Exception e) {
			logger.error("error uploading document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	@Override
	public void deleteAttachment( //
			final Delete delete //
	) throws CMDBException {
		try {
			final String usableIdentifier = (delete.identifier == null) ? generateIdentifier() : delete.identifier;
			final DocumentDelete document = documentCreator(delete.temporary) //
					.createDocumentDelete( //
							EMAIL_CLASS_NAME, //
							usableIdentifier, //
							delete.fileName);
			dmsService.delete(document);
		} catch (final Exception e) {
			logger.error("error deleting document");
			throw DmsException.Type.DMS_ATTACHMENT_DELETE_ERROR.createException();
		}
	}

	@Override
	public Copy copyAttachments( //
			final Copy copy //
	) throws CMDBException {
		try {
			final String usableIdentifier = (copy.identifier == null) ? generateIdentifier() : copy.identifier;
			final DocumentSearch destination = documentCreator(copy.temporary) //
					.createDocumentSearch(EMAIL_CLASS_NAME, usableIdentifier);
			dmsService.create(destination);
			final Map<String, List<CopiableAttachment>> attachmentsByClass = mapByClass(copy.getAttachments());
			for (final List<CopiableAttachment> attachments : attachmentsByClass.values()) {
				for (final CopiableAttachment attachment : attachments) {
					copyAttachment(attachment, destination);
				}
			}
			return copy.modify() //
					.withIdentifier(usableIdentifier) //
					.build();
		} catch (final Exception e) {
			logger.error("error copying document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		}
	}

	private Map<String, List<CopiableAttachment>> mapByClass(final Iterable<CopiableAttachment> attachments) {
		final Map<String, List<CopiableAttachment>> map = newHashMap();
		for (final CopiableAttachment attachment : attachments) {
			final String className = attachment.className;
			final List<CopiableAttachment> attachmentsWithSameClassName;
			if (!map.containsKey(className)) {
				final List<CopiableAttachment> empty = newArrayList();
				map.put(className, empty);
			}
			attachmentsWithSameClassName = map.get(className);
			attachmentsWithSameClassName.add(attachment);
		}
		return map;
	}

	private void copyAttachment(final CopiableAttachment attachment, final DocumentSearch destination) throws DmsError {
		final CMClass sourceClass = dataView.findClass(attachment.className);
		final DocumentSearch source = documentCreatorFactory.create(sourceClass) //
				.createDocumentSearch(attachment.className, attachment.cardId.toString());
		for (final StoredDocument storedDocument : dmsService.search(source)) {
			if (storedDocument.getName().equals(attachment.fileName)) {
				dmsService.copy(storedDocument, source, destination);
			}
		}
	}

	private String generateIdentifier() {
		return UUID.randomUUID().toString();
	}

	private DocumentCreator documentCreator(final boolean temporary) {
		final DocumentCreator documentCreator;
		if (temporary) {
			documentCreator = documentCreatorFactory.createTemporary(Arrays.asList(EMAIL_CLASS_NAME));
		} else {
			final CMClass emailClass = dataView.findClass(EMAIL_CLASS_NAME);
			documentCreator = documentCreatorFactory.create(emailClass);
		}
		return documentCreator;
	}

}
