package org.cmdbuild.dms.alfresco.ftp;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;

import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.alfresco.AlfrescoInnerDmsService;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoConstant;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoWebserviceClient;
import org.cmdbuild.dms.documents.DocumentDelete;
import org.cmdbuild.dms.documents.DocumentDownload;
import org.cmdbuild.dms.documents.SingleDocumentSearch;
import org.cmdbuild.dms.documents.StorableDocument;
import org.cmdbuild.dms.exception.DmsException;
import org.cmdbuild.dms.exception.WebserviceException;

public class AlfrescoFtpService extends AlfrescoInnerDmsService {

	public AlfrescoFtpService(final DmsService parent) {
		super(parent);
	}

	private FtpClient getFtpClient() {
		return new AlfrescoFtpClient(getProperties());
	}

	@Override
	public void delete(final DocumentDelete documentDelete) throws DmsException {
		delete(documentDelete.getFileName(), documentDelete.getPath());
	}

	private void delete(final String filename, final List<String> path) throws DmsException {
		getFtpClient().delete(filename, path);
	}

	@Override
	public DataHandler download(final DocumentDownload documentDownload) throws DmsException {
		final String fileName = documentDownload.getFileName();
		final List<String> path = documentDownload.getPath();
		final DataHandler dataHandler = getFtpClient().download(fileName, path);
		return dataHandler;
	}

	@Override
	public void upload(final StorableDocument storableDocument) throws DmsException {
		uploadFile(storableDocument);
		waitForSomeTimeBetweenFtpAndWebserviceOperations();
		try {
			updateProperties(storableDocument);
			updateCategory(storableDocument);
		} catch (final DmsException e) {
			final String filename = storableDocument.getFileName();
			final List<String> path = storableDocument.getPath();
			final String message = String.format("error updating metadata for file '%s' at path '%s'", //
					filename, Arrays.toString(path.toArray()));
			logger.error(message, e);
			delete(filename, path);
			throw e;
		}

	}

	private void uploadFile(final StorableDocument storableDocument) throws DmsException {
		getFtpClient().upload( //
				storableDocument.getFileName(), //
				storableDocument.getInputStream(), //
				storableDocument.getPath() //
				);
	}

	/**
	 * This is very ugly! Old tests shows some problems if Webservice operations
	 * follows immediately FTP operations, so this delay was introduced.
	 */
	private void waitForSomeTimeBetweenFtpAndWebserviceOperations() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			logger.warn("should never happen... so why?", e);
		}
	}

	private void updateProperties(final StorableDocument storableDocument) throws DmsException {
		final AlfrescoWebserviceClient client = AlfrescoWebserviceClient.getInstance(getProperties());
		final String uuid = getUuid(storableDocument);
		final boolean updated = client.update(uuid, updatableProperties(storableDocument), aspectsProperties());
		if (!updated) {
			final String message = String.format("error updating file '%s' for card '%s' with class '%s'",
					storableDocument.getFileName(), storableDocument.getCardId(), storableDocument.getClassName());
			logger.error(message);
		}
	}

	private static Properties updatableProperties(final StorableDocument storableDocument) {
		final Properties properties = new Properties();
		properties.setProperty(Constants.PROP_TITLE, storableDocument.getFileName());
		properties.setProperty(Constants.PROP_DESCRIPTION, storableDocument.getDescription());
		properties.setProperty(AlfrescoConstant.AUTHOR.getName(), storableDocument.getAuthor());
		return properties;
	}

	private static Properties aspectsProperties() {
		final Properties properties = new Properties();
		properties.setProperty(Constants.ASPECT_VERSIONABLE, "1");
		return properties;
	}

	private void updateCategory(final StorableDocument storableDocument) throws DmsException {
		final AlfrescoWebserviceClient client = AlfrescoWebserviceClient.getInstance(getProperties());
		Reference categoryReference = client.getCategoryReference(storableDocument.getCategory());
		if (categoryReference == null) {
			final boolean categoryCreated = client.createCategory(storableDocument.getCategory());
			if (!categoryCreated) {
				final String message = String.format("error creating category '%s'", storableDocument.getCategory());
				throw new WebserviceException(message);
			}
			categoryReference = client.getCategoryReference(storableDocument.getCategory());
			if (categoryReference == null) {
				final String message = String.format("error getting new category '%s'", storableDocument.getCategory());
				throw new WebserviceException(message);
			}
		}
		final String uuid = getUuid(storableDocument);
		client.applyCategory(categoryReference, uuid);
	}

	private String getUuid(final StorableDocument storableDocument) throws DmsException {
		final AlfrescoWebserviceClient client = AlfrescoWebserviceClient.getInstance(getProperties());
		final ResultSetRow resultSetRow = client.search(singleDocumentSearchFrom(storableDocument));
		final String uuid = resultSetRow.getNode().getId();
		return uuid;
	}

	private static SingleDocumentSearch singleDocumentSearchFrom(final StorableDocument storableDocument) {
		return new SingleDocumentSearch() {

			public String getClassName() {
				return storableDocument.getClassName();
			}

			public int getCardId() {
				return storableDocument.getCardId();
			}

			public List<String> getPath() {
				return storableDocument.getPath();
			}

			public String getFileName() {
				return storableDocument.getFileName();
			}

		};
	}

}
