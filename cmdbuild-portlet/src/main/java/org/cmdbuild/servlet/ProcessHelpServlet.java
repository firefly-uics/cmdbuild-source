package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;

public class ProcessHelpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = "classname";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final ServletOperation operations = new ServletOperation();
		final HttpSession session = request.getSession(true);
		final SOAPClient client = operations.getClient(session);
		final ProcessOperation operation = new ProcessOperation(client);
		final String classname = request.getParameter(CLASSNAME);

		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		try {
			String helpstring = "<p>";
			final String helpText = operation.getProcessHelp(classname);
			if (helpText != null && helpText.length() > 0) {
				helpstring = helpstring + helpText + "</p>";

			} else {
				helpstring = helpstring + "Nessun aiuto disponibile</p>";
			}
			out.write(helpstring);
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
