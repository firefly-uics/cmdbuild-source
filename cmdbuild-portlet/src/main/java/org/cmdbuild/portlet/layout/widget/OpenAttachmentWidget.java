package org.cmdbuild.portlet.layout.widget;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.Types;
import org.cmdbuild.portlet.configuration.AttachmentConfiguration;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.layout.ButtonLayout;
import org.cmdbuild.portlet.operation.AttachmentOperation;
import org.cmdbuild.portlet.operation.LookupOperation;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.servlet.WorkflowWidgetServlet;

public class OpenAttachmentWidget extends WorkflowWidget {

	private static final String ATTACHMENT_FILE = "CMDBuildAttachmentFile";
	private static final String ATTACHMENT_DESCRIPTION = "CMDBuildAttachmenDescription";
	private static final String ATTACHMENT_LOOKUP = "CMDBuildAttachmentLookup";
	public static final String ATTACHMENT_CLASSNAME = "CMDBuildAttachmentClassname";
	public static final String ATTACHMENT_CARDID = "CMDBuildAttachmentCardId";

	public OpenAttachmentWidget(final WorkflowWidgetDefinition definition) {
		super(definition);
	}

	@Override
	public String generateHtml(final HttpServletRequest request) {
		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final SOAPClient client = operations.getClient(request.getSession());
		final StringBuffer layout = new StringBuffer();
		final CardUtils utils = new CardUtils();
		final CardConfiguration config = utils.getCardConfiguration(request);
		layout.append("<div id=\"CMDBuildAttachmentContainer\" class=\"CMDBuildProcessContainer\">");
		layout
				.append("<form action=\"upload\" enctype=\"multipart/form-data\" method=\"post\" class=\"CMDBuildWorkflowWidgetForm\">");
		appendHiddenIdentifier(layout);
		layout.append(addHiddenFieldClassname(config.getClassname()));
		if (config.getId() > 0) {
			layout.append(addHiddenFieldCardId(config.getId()));
		}
		layout.append("<div class=\"CMDBuildAttachmentform\">");
		layout.append(generateAttachmentLookup(client));
		layout.append(generateAttachmentInputFile());
		layout.append(generateAttachmentTextArea());
		final ButtonLayout buttonLayout = new ButtonLayout();
		layout.append(buttonLayout.generateButtons(Types.Buttons.ATTACHMENT_FORM, config, true));
		layout.append("</div>");
		layout.append("</form>");
		layout.append(attachmentGrid());
		layout.append("</div>");
		return layout.toString();
	}

	private String attachmentGrid() {
		final StringBuilder layout = new StringBuilder();
		layout.append("<div class=\"CMDBuildAttachmentGridContainer\">");
		layout.append("<table class=\"CMDBuildAttachmentGrid\">");
		layout.append("</table>");
		layout.append("</div>");
		return layout.toString();
	}

	private String addHiddenFieldCardId(final int id) {
		final StringBuffer layout = new StringBuffer();
		layout.append("<input type=\"hidden\" name=\"").append(ATTACHMENT_CARDID).append("\" value=\"").append(id)
				.append("\" />");
		return layout.toString();
	}

	private String addHiddenFieldClassname(final String classname) {
		final StringBuffer layout = new StringBuffer();
		layout.append("<input type=\"hidden\" name=\"").append(ATTACHMENT_CLASSNAME).append("\" value=\"").append(
				classname).append("\" />");
		return layout.toString();

	}

	private String generateAttachmentTextArea() {
		final StringBuffer layout = new StringBuffer();
		layout
				.append("<div class=\"CMDBuildAttachmentrow\">")
				.append("<div class=\"CMDBuildAttachmentcol1\">")
				.append("<label class=\"CMDBuildAttachmentcategorytitle\">Descrizione: </label>")
				.append("</div>")
				.append("<div class=\"CMDBuildAttachmentcol2\">")
				.append(
						"<textarea rows=\"3\" style=\"width: 100%;\" id=\"CMDBuildAttachmentdescription\" class=\"CMDBuildAttachmentdescription required\" name=\"")
				.append(ATTACHMENT_DESCRIPTION).append("\"/>").append("</div>").append("</div>");
		return layout.toString();
	}

	private String generateAttachmentInputFile() {
		final StringBuffer layout = new StringBuffer();
		layout.append("<div class=\"CMDBuildAttachmentrow\">").append("<div class=\"CMDBuildAttachmentcol1\">").append(
				"<label class=\"CMDBuildAttachmentcategorytitle\">File: </label>").append("</div>").append(
				"<div class=\"CMDBuildAttachmentcol2\">").append(
				"<div id=\"CMDBuildAttachmentfilecontainer\" class=\"CMDBuildAttachmentfile\">").append(
				"<input type=\"file\" id=\"CMDBuildDMSAttachment\" name=\"").append(ATTACHMENT_FILE).append("\"/>")
				.append("</div>").append("</div>").append("</div>");
		return layout.toString();
	}

	private String generateAttachmentLookup(final SOAPClient client) {
		final LookupOperation operation = new LookupOperation(client);
		final List<Lookup> CMDBuildAttachmentLookup = operation.getLookupList(PortletConfiguration.getInstance()
				.getDMSLookup());
		final StringBuffer layout = new StringBuffer();
		layout.append("<div class=\"CMDBuildAttachmentrow\">").append("<div class=\"CMDBuildAttachmentcol1\">").append(
				"<label class=\"CMDBuildAttachmentcategorytitle\">Categoria: </label>").append("</div>").append(
				"<div class=\"CMDBuildAttachmentcol2\">").append("<select id=\"CMDBuildDMSLookup\" name=\"").append(
				ATTACHMENT_LOOKUP).append("\" class\"required\">");
		String attachmentLookup = "";
		if (CMDBuildAttachmentLookup != null) {
			for (final Lookup lookup : CMDBuildAttachmentLookup) {
				attachmentLookup = attachmentLookup + "<option value=\"" + lookup.getDescription() + "\">"
						+ lookup.getDescription() + "</option>";
			}
		}
		layout.append(attachmentLookup).append("</select>").append("</div>").append("</div>");
		return layout.toString();
	}

	@Override
	public void manageWidgetSubmission(final HttpServletRequest request, final RequestParams params) {
		final String action = request.getParameter("action");
		if ("delete".equals(action)) {
			deleteAttachment(request);
		} else {
			final File file = params.getFile(ATTACHMENT_FILE);
			if (file != null) {
				final AttachmentConfiguration attachment = new AttachmentConfiguration();
				attachment.setFile(file);
				attachment.setCategory(params.getParameter(ATTACHMENT_LOOKUP));
				attachment.setDescription(params.getParameter(ATTACHMENT_DESCRIPTION));
				attachment.setClassname(params.getParameter(ATTACHMENT_CLASSNAME));
				attachment.setFilename(file.getName());
				final int cardId = Integer.valueOf(StringUtils.defaultIfEmpty(params.getParameter(ATTACHMENT_CARDID),
						"-1"));
				attachment.setCardid(cardId);
				WorkflowWidgetServlet.getCurrentAttachmentObject(request).add(attachment);
			}
		}
	}

	@Override
	public boolean extraWorkflowWidgetUpdate(final HttpServletRequest request, final int id) {
		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(request.getSession());
		final AttachmentOperation op = new AttachmentOperation(client);
		final List<AttachmentConfiguration> attchmentList = WorkflowWidgetServlet.getCurrentAttachmentObject(request);
		boolean result = true;
		for (final AttachmentConfiguration attachment : attchmentList) {
			try {
				result |= op.uploadAttachment(attachment.getClassname(), id, attachment.getCategory(), attachment
						.getDescription(), attachment.getFilename(), attachment.getFile());
			} catch (final RemoteException ex) {
				Log.PORTLET.warn("Error calling webservice", ex);
				result = false;
			}
		}
		return result;
	}

	@Override
	public void cleanup(final HttpServletRequest request) {
		final List<AttachmentConfiguration> attachmentList = WorkflowWidgetServlet.getCurrentAttachmentObject(request);
		for (final AttachmentConfiguration attachment : attachmentList) {
			attachment.getFile().delete();
		}
	}

	public void deleteAttachment(final HttpServletRequest request) {
		final String filename = request.getParameter("filename");
		final String classname = request.getParameter(ATTACHMENT_CLASSNAME);
		final String cardid = request.getParameter(ATTACHMENT_CARDID);
		if (!"undefined".equals(classname) && !"undefined".equals(cardid)) {
			final HttpSession session = request.getSession();
			final ServletOperation operations = new ServletOperation();
			final SOAPClient client = operations.getClient(session);
			final AttachmentOperation operation = new AttachmentOperation(client);
			operation.deleteAttachment(classname, Integer.valueOf(cardid), filename);
		}
		final List<AttachmentConfiguration> attachmentList = WorkflowWidgetServlet.getCurrentAttachmentObject(request);
		for (final AttachmentConfiguration attachment : attachmentList) {
			if (attachment.getFilename().equals(filename)) {
				attachmentList.remove(attachment);
				attachment.getFile().delete();
			}
		}
	}
}
