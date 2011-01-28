package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;

public class CardServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession(true);
        SOAPClient client = operations.getClient(session);
        CardOperation operation = new CardOperation(client);
        Card card = operations.prepareCard(request);
        PrintWriter writer = response.getWriter();
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
