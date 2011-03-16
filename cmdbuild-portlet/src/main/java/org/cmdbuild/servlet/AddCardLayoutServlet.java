package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.layout.PortletLayout;
import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.ActivitySchema;
import org.cmdbuild.servlet.util.SessionAttributes;

public class AddCardLayoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String TYPE_CARD = "card";
	private static final String TYPE_PROCESS = "process";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final HttpSession session = request.getSession();
		final SOAPClient client = operations.getClient(session);

		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		final String email = StringUtils.defaultIfEmpty((String) session.getAttribute(SessionAttributes.EMAIL),
				StringUtils.EMPTY);
		Log.PORTLET.debug("Connected user email: " + email);
		final CardUtils utils = new CardUtils();
		final CardConfiguration cardConfig = utils.getCardConfiguration(request);
		StringBuilder layout = new StringBuilder();
		final PortletLayout portletLayout = new PortletLayout(client, email, request.getContextPath());
		if (TYPE_CARD.equals(cardConfig.getType())) {
			final FormSerializer form = new FormSerializer(request.getContextPath());
			layout.append(portletLayout.generateTitle(cardConfig.getClassdescription()));
			layout = form.generateEmptyCardLayout(client, cardConfig, cardConfig.getClassname(), false);
		} else if (TYPE_PROCESS.equals(cardConfig.getType())) {
			final ProcessOperation operation = new ProcessOperation(client);
			final FormSerializer form = new FormSerializer(request.getContextPath());
			try {
				final ActivitySchema activity = operation.getActivity(cardConfig.getClassname());
				layout = form.generateProcessLayout(activity, request, false);
			} catch (Exception e) {
				layout = form.generateCannotStartForm();
			}
		}
		try {
			out.write(emptyJavascriptServerVars());
			out.write(layout.toString());
			out.flush();
		} finally {
			out.close();
		}
	}

	private String emptyJavascriptServerVars() {
		final StringBuilder serverVars = new StringBuilder();
		serverVars.append("<script>");
		serverVars.append("var CMDBuildServerVars = {};");
		serverVars.append("</script>");
		return serverVars.toString();
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
