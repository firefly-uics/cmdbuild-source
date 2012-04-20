package org.cmdbuild.dms.alfresco;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.BaseDmsService;
import org.cmdbuild.dms.alfresco.ftp.AlfrescoFtpService;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoWebserviceService;
import org.cmdbuild.dms.documents.DocumentDelete;
import org.cmdbuild.dms.documents.DocumentDownload;
import org.cmdbuild.dms.documents.DocumentSearch;
import org.cmdbuild.dms.documents.DocumentUpdate;
import org.cmdbuild.dms.documents.StorableDocument;
import org.cmdbuild.dms.documents.StoredDocument;
import org.cmdbuild.dms.exception.DmsException;

public class AlfrescoDmsService extends BaseDmsService {

	public void delete(final DocumentDelete document) throws DmsException {
		final AlfrescoInnerDmsService dmsService = new AlfrescoFtpService(this);
		dmsService.delete(document);
	}

	public DataHandler download(final DocumentDownload document) throws DmsException {
		final AlfrescoInnerDmsService dmsService = new AlfrescoFtpService(this);
		return dmsService.download(document);
	}

	public List<StoredDocument> search(final DocumentSearch document) {
		final AlfrescoInnerDmsService dmsService = new AlfrescoWebserviceService(this);
		return dmsService.search(document);
	}

	public void updateDescription(final DocumentUpdate document) throws DmsException {
		final AlfrescoInnerDmsService dmsService = new AlfrescoWebserviceService(this);
		dmsService.updateDescription(document);
	}

	public void upload(final StorableDocument document) throws DmsException {
		final AlfrescoInnerDmsService dmsService = new AlfrescoFtpService(this);
		dmsService.upload(document);
	}

}
