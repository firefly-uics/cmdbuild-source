package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
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

import com.google.common.base.Function;

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

	private final CMDataView dataView;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final OperationUser operationUser;

	public DefaultEmailAttachmentsLogic( //
			final CMDataView dataView, //
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final OperationUser operationUser //
	) {
		this.dataView = dataView;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = new ConfigurationAwareDmsService(dmsService, dmsConfiguration);
		this.documentCreatorFactory = documentCreatorFactory;
		this.operationUser = operationUser;
	}

	@Override
	public void upload(final Long emailId, final boolean temporary, final DataHandler dataHandler) throws CMDBException {
		InputStream inputStream = null;
		try {
			inputStream = dataHandler.getInputStream();
			final StorableDocument document = documentCreator(temporary) //
					.createStorableDocument( //
							operationUser.getAuthenticatedUser().getUsername(), //
							EMAIL_CLASS_NAME, //
							emailId, //
							inputStream, //
							dataHandler.getName(), //
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
	public void copy(final Long emailId, final boolean temporary, final Attachment attachment) throws CMDBException {
		try {
			final DocumentSearch destination = documentCreator(temporary) //
					.createDocumentSearch(EMAIL_CLASS_NAME, emailId);
			dmsService.create(destination);
			final CMClass sourceClass = dataView.findClass(attachment.getClassName());
			final DocumentSearch source = documentCreatorFactory.create(sourceClass) //
					.createDocumentSearch(attachment.getClassName(), attachment.getCardId());
			for (final StoredDocument storedDocument : dmsService.search(source)) {
				if (storedDocument.getName().equals(attachment.getFileName())) {
					dmsService.copy(storedDocument, source, destination);
				}
			}
		} catch (final Exception e) {
			logger.error("error copying document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		}
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

	@Override
	public Iterable<Attachment> readAll(final Long emailId, final boolean temporary) throws CMDBException {
		try {
			final DocumentSearch destination = documentCreator(temporary) //
					.createDocumentSearch(EMAIL_CLASS_NAME, emailId);
			final List<StoredDocument> documents = dmsService.search(destination);
			return from(documents) //
					.transform(new Function<StoredDocument, Attachment>() {

						@Override
						public Attachment apply(final StoredDocument input) {
							return AttachmentImpl.newInstance() //
									.withClassName(EMAIL_CLASS_NAME) //
									.withCardId(emailId) //
									.withFileName(input.getName()) //
									.build();
						}

					});
		} catch (final Exception e) {
			logger.error("error reading documents");
			throw DmsException.Type.DMS_ATTACHMENT_NOTFOUND.createException();
		}

	}

	@Override
	public void delete(final Long emailId, final boolean temporary, final String fileName) throws CMDBException {
		try {
			final DocumentDelete document = documentCreator(temporary) //
					.createDocumentDelete(EMAIL_CLASS_NAME, emailId, fileName);
			dmsService.delete(document);
		} catch (final Exception e) {
			logger.error("error deleting document");
			throw DmsException.Type.DMS_ATTACHMENT_DELETE_ERROR.createException();
		}
	}

}
