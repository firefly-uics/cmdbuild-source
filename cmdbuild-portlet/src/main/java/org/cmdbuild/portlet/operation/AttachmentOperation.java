package org.cmdbuild.portlet.operation;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.AttachmentConfiguration;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attachment;

public class AttachmentOperation extends WSOperation {

	public AttachmentOperation(final SOAPClient client) {
		super(client);
	}

	public boolean uploadAttachment(final String classname, final int cardid, final String category,
			final String description, final String filename, final File file) throws RemoteException {
		Log.PORTLET.debug("Uploading attachment with following parameters");
		Log.PORTLET.debug("Classname: " + classname);
		Log.PORTLET.debug("Card ID: " + cardid);
		Log.PORTLET.debug("Category: " + category);
		Log.PORTLET.debug("Description: " + description);
		Log.PORTLET.debug("Filename: " + filename);
		final DataSource data = new FileDataSource(file);
		final DataHandler handler = new DataHandler(data);
		return getService().uploadAttachment(classname, cardid, handler, filename, category, description);
	}

	public List<AttachmentConfiguration> getAttachmentConfigurationList(final String classname, final int cardid) {

		final List<Attachment> attachmentListResponse = getService().getAttachmentList(classname, cardid);
		if (attachmentListResponse != null && attachmentListResponse.size() > 0) {
			final List<AttachmentConfiguration> attachmentConfigurationList = new ArrayList<AttachmentConfiguration>();
			for (final Attachment attachment : attachmentListResponse) {
				final AttachmentConfiguration attachmentConfiguration = convertToAttachmentConfiguration(attachment,
						classname, cardid);
				attachmentConfigurationList.add(attachmentConfiguration);
			}
			return attachmentConfigurationList;
		} else {
			return new ArrayList<AttachmentConfiguration>();
		}

	}

	private AttachmentConfiguration convertToAttachmentConfiguration(final Attachment originalAttachment,
			final String classname, final int cardid) {
		final AttachmentConfiguration attachment = new AttachmentConfiguration();
		attachment.setCardid(cardid);
		attachment.setCategory(originalAttachment.getCategory());
		attachment.setClassname(classname);
		attachment.setDescription(originalAttachment.getDescription());
		attachment.setFilename(originalAttachment.getFilename());
		return attachment;
	}

	public DataHandler downloadAttachment(final String classname, final int cardid, final String filename) {
		return getService().downloadAttachment(classname, cardid, filename);
	}

	public boolean deleteAttachment(final String classname, final int cardid, final String filename) {
		return getService().deleteAttachment(classname, cardid, filename);
	}
}
