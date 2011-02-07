package org.cmdbuild.servlet;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.operation.AttachmentOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;

public class DownloadAttachmentServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = "classname";
	private static final String CARDID = "cardid";
	private static final String FILENAME = "filename";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final HttpSession session = request.getSession();
		final SOAPClient client = operations.getClient(session);
		final AttachmentOperation operation = new AttachmentOperation(client);

		final String classname = request.getParameter(CLASSNAME);
		final int cardid = Integer.valueOf(request.getParameter(CARDID));
		final String filename = request.getParameter(FILENAME);
		final DataHandler data = operation.downloadAttachment(classname, cardid, filename);
		response.setContentType(data.getContentType());
		response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\";", filename));
		response.setHeader("Expires", "0");
		data.writeTo(response.getOutputStream());
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
