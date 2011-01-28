package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;

public class ModifyCardServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession(true);
        SOAPClient client = operations.getClient(session);
        CardOperation operation = new CardOperation(client);
        Card card = operations.prepareCard(request);
        PrintWriter writer = response.getWriter();
        boolean tmpresult;
        tmpresult = operation.updateCard(card);
        if (tmpresult) {
            writer.append("<p>Card aggiornata correttamente</p>");
        } else {
            writer.append("<p>Errore in fase di aggiornamento della card</p>");
        }
        writer.flush();
        writer.close();
    }
}
