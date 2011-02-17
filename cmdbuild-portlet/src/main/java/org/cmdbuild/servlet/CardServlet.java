package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;

public class CardServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final ServletOperation operations = new ServletOperation();
		final HttpSession session = request.getSession(true);
		final SOAPClient client = operations.getClient(session);
		final CardOperation operation = new CardOperation(client);
		final Card card = operations.prepareCard(request);
		final PrintWriter writer = response.getWriter();
		int tmpresult;
		tmpresult = operation.createCard(card);
		if (tmpresult > 0) {
			writer.append("<p>Card creata correttamente</p>");
		} else {
			writer.append("<p>Errore in fase di creazione della card</p>");
		}
		writer.flush();
		writer.close();
	}
}
