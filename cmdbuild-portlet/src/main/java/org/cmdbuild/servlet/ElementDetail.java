package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.layout.PortletLayout;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.servlet.util.SessionAttributes;

public class ElementDetail extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = "classname";
	private static final String CARDID = "cardid";
	private static final String INDEX = "index";

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final ServletOperation operations = new ServletOperation();
		final HttpSession session = request.getSession(true);
		final SOAPClient client = operations.getClient(session);
		final CardOperation operation = new CardOperation(client);

		response.setContentType("text/html;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		final String classname = request.getParameter(CLASSNAME);
		final String email = StringUtils.defaultIfEmpty((String) session.getAttribute(SessionAttributes.EMAIL),
				StringUtils.EMPTY);
		final int cardid = Integer.parseInt(request.getParameter(CARDID));
		final int index = Integer.parseInt(request.getParameter(INDEX));
		final List<AttributeSchema> schema = operation.getAttributeList(classname);

		final Map<Integer, Card> cards = new LinkedHashMap<Integer, Card>();
		final CardList cardlist = operation.getCardHistory(classname, cardid, 0, 0);
		for (int i = 0; i < cardlist.getCards().size(); i++) {
			cards.put(i, cardlist.getCards().get(i));
		}
		final PortletLayout layout = new PortletLayout(client, email, request.getContextPath());
		out.write(layout.generateElementDetail(index, cards, schema));
		out.flush();
		out.close();
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
