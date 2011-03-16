package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.ButtonBarConfiguration;
import org.cmdbuild.portlet.layout.ButtonLayout;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.utils.ButtonUtils;
import org.cmdbuild.servlet.util.SessionAttributes;

public class ButtonBarServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		final boolean advance = Boolean.valueOf(StringUtils.defaultIfEmpty(request
				.getParameter(SessionAttributes.ADVANCE_PROCESS), "false"));
		final ButtonUtils buttonUtils = new ButtonUtils();
		final ButtonBarConfiguration buttonBarConfiguration = buttonUtils.generateButtonConfiguration(request);
		final StringBuilder layout = new StringBuilder();
		final ButtonLayout buttonLayout = new ButtonLayout();
		final List<WorkflowWidget> wwList = WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession());
		layout.append(buttonLayout.generateProcessButtonBar(buttonBarConfiguration, wwList, advance));
		try {
			out.write(layout.toString());
			out.flush();
		} finally {
			out.close();
		}
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
