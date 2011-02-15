package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.portlet.layout.LookupComponentSerializer;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;

public class AddProcessFlowStatus extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = "classname";
	private static final String FLOWSTATUS = "flowstatus";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final ServletOperation operations = new ServletOperation();
		final HttpSession session = request.getSession(true);
		final SOAPClient client = operations.getClient(session);
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		Log.PORTLET.debug("Creating flow status html code");
		final String classname = request.getParameter(CLASSNAME);
		Log.PORTLET.debug("- Classname: " + classname);
		final String flowstatus = request.getParameter(FLOWSTATUS);
		Log.PORTLET.debug("Displaying processes with id " + flowstatus);
		try {
			final ComponentLayout layout = new ComponentLayout();
			layout.setClient(client);
			final LookupComponentSerializer lookupLayout = new LookupComponentSerializer(layout);
			final String statoprocesso = lookupLayout.addFlowStatusLookup(flowstatus, classname);
			out.write(statoprocesso);
			out.flush();
		} finally {
			out.close();
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		processRequest(req, resp);
	}
}
