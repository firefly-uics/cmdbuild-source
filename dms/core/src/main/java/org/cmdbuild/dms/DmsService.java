package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.documents.DocumentDelete;
import org.cmdbuild.dms.documents.DocumentDownload;
import org.cmdbuild.dms.documents.DocumentSearch;
import org.cmdbuild.dms.documents.DocumentUpdate;
import org.cmdbuild.dms.documents.StorableDocument;
import org.cmdbuild.dms.documents.StoredDocument;
import org.cmdbuild.dms.exception.DmsException;
import org.cmdbuild.dms.properties.DmsProperties;

public interface DmsService {

	DmsProperties getProperties();

	void setProperties(DmsProperties dmsProperties);

	List<StoredDocument> search(DocumentSearch document);

	void upload(StorableDocument document) throws DmsException;

	DataHandler download(DocumentDownload document) throws DmsException;

	void delete(DocumentDelete document) throws DmsException;

	void updateDescription(DocumentUpdate document) throws DmsException;

}
