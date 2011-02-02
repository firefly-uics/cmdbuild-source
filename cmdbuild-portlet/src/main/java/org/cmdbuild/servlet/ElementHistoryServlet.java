package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;

public class ElementHistoryServlet extends HttpServlet {

    private static final String CLASSNAME = "classname";
    private static final String TYPE = "type";
    private static final String CARDID = "cardid";
    private static final String PAGE = "page";
    private static final String MAXRESULT = "rp";
    private String contextPath;


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession(true);
        SOAPClient client = operations.getClient(session);
        this.contextPath = (String) session.getAttribute("contextPath");
        String classname = request.getParameter(CLASSNAME);
        String type = request.getParameter(TYPE);
        int cardid = Integer.parseInt(request.getParameter(CARDID));
        String page = StringUtils.defaultIfEmpty(request.getParameter(PAGE), "1");
        String maxResult = StringUtils.defaultIfEmpty(request.getParameter(MAXRESULT), "10");

        response.setContentType("text/xml");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        String pageString = StringUtils.defaultIfEmpty(page, "1");
        int ipage = Integer.parseInt(pageString);
        String limitString = StringUtils.defaultIfEmpty(maxResult, "10");
        int rpage = Integer.parseInt(limitString);
        int startIndex = (ipage - 1) * rpage;

        PrintWriter out = response.getWriter();
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.println("<rows>");
        out.println("<page>" + ipage + "</page>");
        try {
            CardList cardlist = getCardInfo(client, classname, cardid, startIndex, rpage);
            if (cardlist != null && cardlist.getCards().size() > 0) {
                out.println(getXMLString(cardlist, type));
            } else {
                out.println("<total>0</total>\n");
            }
            out.println("</rows>");
        } finally {
            out.flush();
            out.close();
        }
    }

    private CardList getCardInfo(SOAPClient client, String classname, int cardid, int startIndex, int maxResult) {
        CardOperation operation = new CardOperation(client);
        CardList cards = operation.getCardHistory(classname, cardid, maxResult, startIndex);
        return cards;
    }

    private String getXMLString(CardList list, String type) {

        String result = "";
        if (list != null) {
            int total = list.getTotalRows();
            result = "<total>" + total + "</total>\n";

            for (int i = 0; i < list.getCards().size(); i++) {
                Card card = list.getCards().get(i);
                List<Attribute> attrs = card.getAttributeList();
                StringBuilder row = new StringBuilder();
                row.append(String.format("<row id='%d'>\n", card.getId()));
                row.append(String.format("<cell><![CDATA[%d]]></cell>\n", card.getId()));
                row.append(generateCell(attrs, "BeginDate"));
                row.append(generateEndDateCell(attrs, "EndDate"));
                row.append(generateCell(attrs, "Description"));
                row.append(String.format("<cell><![CDATA[<img src=\"%s/css/images/zoom.png\" alt=\"Storia\" " +
                        "onclick=\"CMDBuildShowElementDetail('%s', '%s', '%d', '%s')\" />]]></cell>\n", contextPath, i, card.getClassName(), card.getId(), type));
                row.append("</row>\n");
                result = result + row.toString();
            }
        } else {
            StringBuilder row = new StringBuilder();
            row.append(String.format("<total>%s</total>\n", 0));
            row.append(String.format("<row id='%s'>\n", 0));
            row.append("</row>\n");
            result = result + row.toString();
        }
        return result;
    }

    private String generateCell(List<Attribute> attrs, String value) {
        String result = "";
        if (attrs != null && attrs.size() > 0) {
            for (Attribute attribute : attrs) {
                String aname = attribute.getName();
                String avalue = attribute.getValue();
                if (aname.equals(value)) {
                    result = result + "<cell><![CDATA[" + avalue + "]]></cell>\n";
                } 
            }
        }
        return result;
    }

    private String generateEndDateCell(List<Attribute> attrs, String string) {
        String result = "";
        String endDateValue = "";
        boolean isEndDate = false;
        for (Attribute a : attrs){
            if (a.getName().equals("EndDate")){
                isEndDate = true;
                endDateValue = a.getValue();
            }
        }
        if (isEndDate){
            result = result + "<cell><![CDATA[" + endDateValue + "]]></cell>\n";
        } else {
            result = result + "<cell></cell>\n";
        }
        return result;
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
