package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
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
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;

public class ElementDetail extends HttpServlet {

    private static final String EMAIL = "useremail";
    private static final String CLASSNAME = "classname";
    private static final String CARDID = "cardid";
    private static final String INDEX = "index";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession(true);
        SOAPClient client = operations.getClient(session);
        CardOperation operation = new CardOperation(client);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String classname = request.getParameter(CLASSNAME);
        String email = StringUtils.defaultIfEmpty((String) session.getAttribute(EMAIL), "");
        int cardid = Integer.parseInt(request.getParameter(CARDID));
        int index = Integer.parseInt(request.getParameter(INDEX));
        List<AttributeSchema> schema = operation.getAttributeList(classname);

        Map<Integer, Card> cards = new LinkedHashMap<Integer, Card>();
        CardList cardlist = operation.getCardHistory(classname, cardid, 0, 0);
        for (int i=0; i<cardlist.getCards().size(); i++) {
            cards.put(i, cardlist.getCards().get(i));
        }
        PortletLayout layout = new PortletLayout(client, email, request.getContextPath());
        out.write(layout.generateElementDetail(index, cards, schema));
        out.flush();
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
