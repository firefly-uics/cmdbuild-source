package org.cmdbuild.dms.alfresco.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.Constants;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.alfresco.AlfrescoInnerDmsService;
import org.cmdbuild.dms.alfresco.StoredDocumentComparator;
import org.cmdbuild.dms.documents.DocumentSearch;
import org.cmdbuild.dms.documents.DocumentUpdate;
import org.cmdbuild.dms.documents.StoredDocument;
import org.cmdbuild.dms.exception.DmsException;
import org.cmdbuild.dms.exception.WebserviceException;

public class AlfrescoWebserviceService extends AlfrescoInnerDmsService {

	public AlfrescoWebserviceService(final DmsService parent) {
		super(parent);
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) {
		final AlfrescoWebserviceClient client = AlfrescoWebserviceClient.getInstance(getProperties());
		final ResultSetRow[] resultSetRows = client.search(document);
		final List<StoredDocument> storedDocuments = new ArrayList<StoredDocument>();
		for (final ResultSetRow resultSetRow : resultSetRows) {
			final StoredDocument storedDocument = storedDocument(resultSetRow);
			storedDocuments.add(storedDocument);
		}
		Collections.sort(storedDocuments, StoredDocumentComparator.INSTANCE);
		return storedDocuments;
	}

	private StoredDocument storedDocument(final ResultSetRow resultSetRow) {
		final StoredDocument storedDocument = new StoredDocument();
		final NamedValue[] namedValues = resultSetRow.getColumns();
		final AlfrescoWebserviceClient client = AlfrescoWebserviceClient.getInstance(getProperties());
		for (final NamedValue namedValue : namedValues) {
			final AlfrescoConstant alfrescoConstant = AlfrescoConstant.fromName(namedValue.getName());
			alfrescoConstant.setInBean(storedDocument, namedValue, client);
		}
		return storedDocument;
	}

	@Override
	public void updateDescription(final DocumentUpdate document) throws DmsException {
		final List<StoredDocument> storedDocuments = search(new DocumentSearch() {

			public String getClassName() {
				return document.getClassName();
			}

			public int getCardId() {
				return document.getCardId();
			}

			public List<String> getPath() {
				return document.getPath();
			}

		});

		for (final StoredDocument storedDocument : storedDocuments) {
			if (storedDocument.getName().equals(document.getFileName())) {
				final String uuid = storedDocument.getUuid();
				final AlfrescoWebserviceClient client = AlfrescoWebserviceClient.getInstance(getProperties());
				final Properties updatableProperties = new Properties();
				updatableProperties.setProperty(Constants.PROP_DESCRIPTION, document.getDescription());
				final Properties EMPTY_PROPERTIES = new Properties();
				final boolean updated = client.update(uuid, updatableProperties, EMPTY_PROPERTIES);
				if (!updated) {
					throw new WebserviceException();
				}
			}
		}
	}

}
