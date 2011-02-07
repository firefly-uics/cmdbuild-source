package org.cmdbuild.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.AttachmentConfiguration;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.operation.RequestParams;

public class WorkflowWidgetServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String WW_IDENTIFIER_PARAM = "CMDBuildIdentifier";
	public static final String WW_ATTACHMENT_OBJECT_PARAM = "WW_ATTACHMENT_OBJECT";

	public static List<AttachmentConfiguration> getCurrentAttachmentObject(final HttpServletRequest request) {
		List<AttachmentConfiguration> currentAttachmentObject = (List<AttachmentConfiguration>) request.getSession()
				.getAttribute(WW_ATTACHMENT_OBJECT_PARAM);
		if (currentAttachmentObject == null) {
			currentAttachmentObject = new ArrayList<AttachmentConfiguration>();
			request.getSession().setAttribute(WW_ATTACHMENT_OBJECT_PARAM, currentAttachmentObject);
		}
		return currentAttachmentObject;
	}

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException, Exception {
		response.setContentType("text/html;charset=UTF-8");
		final RequestParams params = RequestParams.create(request);
		final WorkflowWidget ww = WorkflowWidget.getWorkflowWidgetFromSession(request.getSession(), params
				.getParameter(WW_IDENTIFIER_PARAM));
		if (ww != null) {
			ww.manageWidgetSubmission(request, params);
		}
		response.getWriter().print("");
		response.getWriter().flush();
		response.getWriter().close();
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException {
		try {
			processRequest(request, response);
		} catch (final Exception ex) {
			Log.PORTLET.debug("Error handling Workflow Widget", ex);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (final Exception ex) {
			Log.PORTLET.debug("Error handling Workflow Widget", ex);
		}
	}
}
