package org.cmdbuild.dms.alfresco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.cmdbuild.config.LegacydmsProperties;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.FtpClient;
import org.cmdbuild.dms.SingleDocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoConstant;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoWebserviceClient;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

public class AlfrescoDmsService implements DmsService {

	@Override
	public List<StoredDocument> search(DocumentSearch search) {
		final LegacydmsProperties properties = LegacydmsProperties.getInstance();
		final AlfrescoClient client = new AlfrescoWebserviceClient(properties);
		final ResultSetRow[] resultSetRows = client.search(search);
		final List<StoredDocument> storedDocuments = new ArrayList<StoredDocument>();
		for (final ResultSetRow resultSetRow : resultSetRows) {
			final StoredDocument storedDocument = storedDocument(resultSetRow);
			storedDocuments.add(storedDocument);
		}
		Collections.sort(storedDocuments, StoredDocumentComparator.INSTANCE);
		return storedDocuments;
	}

	private static StoredDocument storedDocument(final ResultSetRow resultSetRow) {
		final LegacydmsProperties properties = LegacydmsProperties.getInstance();
		final StoredDocument storedDocument = new StoredDocument();
		final NamedValue[] namedValues = resultSetRow.getColumns();
		final AlfrescoClient client = new AlfrescoWebserviceClient(properties);
		for (final NamedValue namedValue : namedValues) {
			final AlfrescoConstant alfrescoConstant = AlfrescoConstant.fromName(namedValue.getName());
			alfrescoConstant.setInBean(storedDocument, namedValue, client);
		}
		return storedDocument;
	}

	@Override
	public boolean upload(StorableDocument storableDocument) {
		try {
			final LegacydmsProperties properties = LegacydmsProperties.getInstance();
			final FtpClient ftpClient = new AlfrescoFtpClient(properties);
			final String[] path = path(storableDocument.getClassName(), storableDocument.getCardId());
			final boolean uploaded = ftpClient.upload(storableDocument.getFileName(),
					storableDocument.getInputStream(), path);
			if (!uploaded) {
				final String message = String.format("error uploading file '%s' for card '%s' with class '%s'",
						storableDocument.getFileName(), storableDocument.getCardId(), storableDocument.getClassName());
				Log.DMS.error(message);
				return false;
			}
			// hack..
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				Log.DMS.error("error uploading file", e.fillInStackTrace());
				e.printStackTrace();
			}
			//

			final AlfrescoClient client = new AlfrescoWebserviceClient(properties);

			final ResultSetRow resultSetRow = client.search(singleDocumentSearchFrom(storableDocument));
			final String uuid = resultSetRow.getNode().getId();
			final boolean updated = client.update(uuid, updatableProperties(storableDocument), aspectsProperties());
			if (!updated) {
				final String message = String.format("error updating file '%s' for card '%s' with class '%s'",
						storableDocument.getFileName(), storableDocument.getCardId(), storableDocument.getClassName());
				Log.DMS.error(message);
				return false;
			}

			Reference categoryReference = client.getCategoryReference(storableDocument.getCategory());
			if (categoryReference == null) {
				client.createCategory(storableDocument.getCategory());
				categoryReference = client.getCategoryReference(storableDocument.getCategory());
				if (categoryReference == null) {
					final String message = String.format("error getting category '%s'", storableDocument.getCategory());
					Log.DMS.error(message);
					return false;
				}
			}

			client.applyCategory(categoryReference, uuid);

			return true;
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' for card '%s' with class '%s'",
					storableDocument.getFileName(), storableDocument.getCardId(), storableDocument.getClassName());
			Log.DMS.error(message, e);
			return false;
		}
	}

	private static String[] path(final String className, final int classId) throws NotFoundException {
		final Collection<String> classWithAncestors = TableImpl.tree().path(className);
		final String[] path = new String[classWithAncestors.size() + 1];
		classWithAncestors.toArray(path);
		path[classWithAncestors.size()] = "Id" + classId;
		return path;
	}

	private static SingleDocumentSearch singleDocumentSearchFrom(final StorableDocument storableDocument) {
		return new SingleDocumentSearch() {

			@Override
			public String getClassName() {
				return storableDocument.getClassName();
			}

			@Override
			public int getCardId() {
				return storableDocument.getCardId();
			}

			@Override
			public String getFileName() {
				return storableDocument.getFileName();
			}

		};
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

	@Override
	public DataHandler download(DocumentDownload documentDownload) {
		final LegacydmsProperties properties = LegacydmsProperties.getInstance();
		final FtpClient ftpClient = new AlfrescoFtpClient(properties);
		final String[] path = path(documentDownload.getClassName(), documentDownload.getCardId());
		final DataHandler dataHandler = ftpClient.download(documentDownload.getFileName(), path);
		return dataHandler;
	}

	// TODO synchronized
	@Override
	public boolean delete(DocumentDelete delete) {
		final LegacydmsProperties properties = LegacydmsProperties.getInstance();
		final FtpClient client = new AlfrescoFtpClient(properties);
		final String[] path = path(delete.getClassName(), delete.getCardId());
		// hack..
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			Log.DMS.error("error making metadata", e.fillInStackTrace());
			return client.delete(delete.getFileName(), path);
		}
		//
		return client.delete(delete.getFileName(), path);
	}

	@Override
	public boolean updateDescription(final DocumentUpdate update) {
		final List<StoredDocument> storedDocuments = search(new DocumentSearch() {

			@Override
			public String getClassName() {
				return update.getClassName();
			}

			@Override
			public int getCardId() {
				return update.getCardId();
			}

		});

		for (final StoredDocument storedDocument : storedDocuments) {
			if (storedDocument.getName().equals(update.getFileName())) {
				final String uuid = storedDocument.getUuid();
				final LegacydmsProperties properties = LegacydmsProperties.getInstance();
				final AlfrescoClient client = new AlfrescoWebserviceClient(properties);
				final Properties updatableProperties = new Properties();
				updatableProperties.setProperty(Constants.PROP_DESCRIPTION, update.getDescription());
				final Properties EMPTY_PROPERTIES = new Properties();
				return client.update(uuid, updatableProperties, EMPTY_PROPERTIES);
			}
		}
		return false;
	}

}
