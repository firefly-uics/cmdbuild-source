package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.AttachmentConfiguration;
import org.cmdbuild.portlet.operation.AttachmentOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.servlet.util.SessionAttributes;

public class DisplayAttachmentServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = "classname";
	private static final String CARDID = "cardid";
	private static final String DELETE_ATTACHMENT_BUTTON = "deletebutton";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/xml");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		final String contextPath = (String) request.getSession().getAttribute(SessionAttributes.CONTEXT_PATH);
		final Writer writer = response.getWriter();
		final HttpSession session = request.getSession();
		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(session);
		final AttachmentOperation operation = new AttachmentOperation(client);
		final String classname = StringUtils.defaultIfEmpty(request.getParameter(CLASSNAME), "");
		final int cardid = Integer.valueOf(StringUtils.defaultIfEmpty(request.getParameter(CARDID), "0"));
		boolean deletebutton = true;
		if (request.getParameter(DELETE_ATTACHMENT_BUTTON) != null) {
			deletebutton = Boolean.valueOf(request.getParameter(DELETE_ATTACHMENT_BUTTON));
		}
		try {
			List<AttachmentConfiguration> alreadyAttached = new ArrayList<AttachmentConfiguration>();
			if (!"".equals(classname) && cardid > 0) {
				alreadyAttached = operation.getAttachmentConfigurationList(classname, cardid);
			}
			final List<AttachmentConfiguration> attchmentList = WorkflowWidgetServlet
					.getCurrentAttachmentObject(request);
			final String attachmentXML = generateXMLResponse(attchmentList, alreadyAttached, deletebutton, cardid,
					contextPath);
			writer.append(attachmentXML);
		} finally {
			writer.flush();
			writer.close();
			writer.close();
		}
	}

	private String generateXMLResponse(final List<AttachmentConfiguration> attachmentList,
			final List<AttachmentConfiguration> alreadyAttached, final boolean deletebutton, final int cardid,
			final String contextPath) {
		final StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append("<rows>").append("<page>1</page>");
		if (alreadyAttached != null && alreadyAttached.size() > 0) {
			xml.append(generateAttachmentXML(alreadyAttached, true, deletebutton, cardid, contextPath));
		}
		if (attachmentList.size() > 0) {
			xml.append(generateAttachmentXML(attachmentList, false, deletebutton, cardid, contextPath));
		} else {
			xml.append(generateEmptyXML());
		}

		xml.append("</rows>");
		return xml.toString();
	}

	private String generateRow(final AttachmentConfiguration attachment, final int id, final boolean alreadyAttached,
			final boolean deletebutton, final int cardid, final String contextPath) {
		final StringBuilder xml = new StringBuilder();
		xml.append("<row id='").append(id).append("'>");
		xml.append("<cell><![CDATA[").append(attachment.getFilename()).append("]]></cell>");
		xml.append("<cell><![CDATA[").append(attachment.getCategory()).append("]]></cell>");
		xml.append("<cell><![CDATA[").append(attachment.getDescription()).append("]]></cell>");
		xml.append("<cell><![CDATA[").append(
				createAttachmentButtonBar(attachment, alreadyAttached, deletebutton, contextPath)).append("]]></cell>");
		xml.append("</row>");
		return xml.toString();
	}

	private String createAttachmentButtonBar(final AttachmentConfiguration attachment, final boolean downloadFile,
			final boolean deletebutton, final String contextPath) {
		final StringBuilder buttonBar = new StringBuilder();
		buttonBar.append(generateDeleteAttachmentButton(attachment, downloadFile, deletebutton, contextPath));
		if (downloadFile) {
			buttonBar
					.append("<span class=\"CMDBuildGridButton\"><img src=\"")
					.append(contextPath)
					.append(
							"/css/images/page_white_put.png\" alt=\"Download\" title=\"Download\" onclick=\"CMDBuildDownloadAttachment('")
					.append(attachment.getClassname()).append("', '").append(attachment.getCardid()).append("', '")
					.append(attachment.getFilename()).append("')\"/></span>");
		}
		return buttonBar.toString();
	}

	private String generateDeleteAttachmentButton(final AttachmentConfiguration attachment,
			final boolean alreadyAttached, final boolean deletebutton, final String contextPath) {
		final StringBuilder button = new StringBuilder();
		if (deletebutton) {
			if (alreadyAttached) {
				button
						.append("<span class=\"CMDBuildGridButton\"><img src=\"")
						.append(contextPath)
						.append(
								"/css/images/close.png\" alt=\"Cancella\" title=\"Cancella\" onclick=\"CMDBuildDeleteAttachment('")
						.append(attachment.getFilename()).append("', '").append(attachment.getClassname()).append(
								"', '").append(attachment.getCardid()).append("')\"/></span>");
			} else {
				button.append("<span class=\"CMDBuildGridButton\" <img src=\"").append(contextPath).append(
						"/css/images/close.png\" alt=\"Cancella\" onclick=\"CMDBuildDeleteAttachment('").append(
						attachment.getFilename()).append("')\"/>");
			}
		}
		return button.toString();
	}

	private String generateEmptyXML() {
		return "<total>0</total>\n";
	}

	private String generateAttachmentXML(final List<AttachmentConfiguration> attachmentList,
			final boolean alreadyAttached, final boolean deletebutton, final int cardid, final String contextPath) {
		final StringBuffer xml = new StringBuffer();
		xml.append("<total>").append(attachmentList.size()).append("</total>\n");
		int counter = 0;
		for (final AttachmentConfiguration attachment : attachmentList) {
			xml.append(generateRow(attachment, counter, alreadyAttached, deletebutton, cardid, contextPath));
			counter++;
		}
		return xml.toString();
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}
}
