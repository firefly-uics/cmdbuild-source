package org.cmdbuild.services.soap.operation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.legacy.dms.AlfrescoFacade;
import org.cmdbuild.legacy.dms.AttachmentBean;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.types.Attachment;

public class EAttachment {

	private UserContext userCtx;

	public EAttachment(UserContext userCtx) {
		this.userCtx = userCtx;
	}
	
	public Attachment[] getAttachmentList(String className, int objectid) {
		AlfrescoFacade operation = new AlfrescoFacade(userCtx, className, objectid);
		Log.SOAP.debug("I'm going to request the attachment list for object " + objectid + " from classname " + className);
		List<AttachmentBean> beans = operation.search();
		List<Attachment> list = new ArrayList<Attachment>();
		for (AttachmentBean ab : beans) {
			Attachment at = new Attachment(ab);
			list.add(at);
		}
		
		Attachment[] attachments = new Attachment[list.size()];
		attachments = list.toArray(attachments);
		return attachments;
	}

	public boolean upload(String className, int objectid, DataHandler file,
			String filename, String category, String description) {

		try {
			AlfrescoFacade operation = new AlfrescoFacade(userCtx, className, objectid);
			boolean result = operation.upload(file.getInputStream(), filename,
					category, description);
			if (result)
				return true;
			else {
				Log.SOAP.info("ERROR");
				Log.SOAP.debug("Error uploading file " + filename + "in " + className);
			}
		} catch (FileNotFoundException e) {
			Log.SOAP.error(e.getMessage(), e);
			Log.SOAP.debug("Could not find file " + filename, e);
		} catch (IOException e) {
			Log.SOAP.error("I/O exception", e);
			Log.SOAP.debug("I/O exception", e);
		}
		return false;
	}

	public DataHandler download(String className, int objectid, String filename) {
		Log.SOAP.debug("I'm going to download file " + filename + " from object " + objectid);
		AlfrescoFacade operation = new AlfrescoFacade(userCtx, className, objectid);
		return operation.download(filename);

	}

	public boolean deleteAttachment(String className, int cardId,
			String filename) {
		Log.SOAP.debug("I'm going to delete " + filename + "from "+ className + " card " + cardId);
		AlfrescoFacade operation = new AlfrescoFacade(userCtx, className, cardId);
		return operation.delete(filename);

	}

	public boolean updateAttachmentDescription(String className, int cardId,
			String filename, String description) {
		Log.SOAP.debug("I'm going to update " + className + " card with id " + cardId); 
		AlfrescoFacade operation = new AlfrescoFacade(userCtx, className, cardId);
		return operation.updateDescription(filename, description);

	}
}
