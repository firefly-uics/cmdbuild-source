package org.cmdbuild.dms.alfresco;

import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.documents.DocumentDelete;
import org.cmdbuild.dms.documents.DocumentDownload;
import org.cmdbuild.dms.documents.DocumentSearch;
import org.cmdbuild.dms.documents.DocumentUpdate;
import org.cmdbuild.dms.documents.StorableDocument;
import org.cmdbuild.dms.documents.StoredDocument;
import org.cmdbuild.dms.exception.DmsException;
import org.cmdbuild.dms.properties.DmsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlfrescoInnerDmsService implements DmsService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final DmsService parent;

	public AlfrescoInnerDmsService(final DmsService parent) {
		Validate.notNull(parent, "null parent");
		this.parent = parent;
	}

	public DmsProperties getProperties() {
		return parent.getProperties();
	}

	public void setProperties(final DmsProperties dmsProperties) {
		parent.setProperties(dmsProperties);
	}

	public void delete(final DocumentDelete document) throws DmsException {
		throw new UnsupportedOperationException();
	}

	public DataHandler download(final DocumentDownload document) throws DmsException {
		throw new UnsupportedOperationException();
	}

	public List<StoredDocument> search(final DocumentSearch document) {
		throw new UnsupportedOperationException();
	}

	public void updateDescription(final DocumentUpdate document) throws DmsException {
		throw new UnsupportedOperationException();
	}

	public void upload(final StorableDocument document) throws DmsException {
		throw new UnsupportedOperationException();
	}

}
