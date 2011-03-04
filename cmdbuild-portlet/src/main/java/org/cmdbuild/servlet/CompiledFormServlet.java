package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.ActivitySchema;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.servlet.util.SessionAttributes;

public class CompiledFormServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String TYPE_CARD = "card";
	private static final String TYPE_ADVANCEPROCESS = "advance";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final HttpSession session = request.getSession();
		final SOAPClient client = operations.getClient(session);
		Log.PORTLET.debug("Creating compiled form with following parameters");
		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		final CardUtils utils = new CardUtils();
		final CardConfiguration cardConfig = utils.getCardConfiguration(request);
		final String email = StringUtils.defaultIfEmpty((String) session.getAttribute(SessionAttributes.EMAIL),
				StringUtils.EMPTY);
		Log.PORTLET.debug("Connected user email: " + email);
		StringBuilder layout = new StringBuilder();
		final PortletLayout portletLayout = new PortletLayout(client, email, request.getContextPath());
		final FormSerializer form = new FormSerializer(request.getContextPath());
		final ProcessOperation operation = new ProcessOperation(client);
		final CardOperation cardoperation = new CardOperation(client);
		final Card card = cardoperation.getCard(cardConfig.getClassname(), cardConfig.getId());
		if (TYPE_CARD.equals(cardConfig.getType())) {
			layout.append(portletLayout.generateTitle(cardConfig.getClassdescription()));
			layout.append(form.generateCompiledCardLayout(client, cardConfig, false));
		} else if (TYPE_ADVANCEPROCESS.equals(cardConfig.getType())) {
			final ActivitySchema activity = operation.getActivity(cardConfig.getClassname(), cardConfig.getId());
			layout = form.generateProcessLayout(activity, request, true);
		}
		layout.append(javascriptServerVars(card.getAttributeList()));
		out.write(layout.toString());
		out.flush();
		out.close();
	}

	private String javascriptServerVars(final List<Attribute> cardAttributes) {
		final StringBuilder serverVars = new StringBuilder();
		serverVars.append("<script>");
		serverVars.append("var CMDBuildServerVars = { ");
		final int length = cardAttributes.size();
		for (int i = 0; i < length; ++i) {
			final Attribute attribute = cardAttributes.get(i);
			// if is reference or lookup use code
			if (attribute.getCode() != null && !"".equals(attribute.getCode())) {
				serverVars.append(attribute.getName()).append(": \"").append(attribute.getCode()).append("\", ");
				serverVars.append(attribute.getName()).append("_value: \"").append(attribute.getValue());
			} else {
				serverVars.append(attribute.getName()).append(": \"").append(attribute.getValue());
			}
			if (i < length) {
				serverVars.append("\", ");
			}
		}
		serverVars.append("};");
		serverVars.append("</script>");
		return serverVars.toString();
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
