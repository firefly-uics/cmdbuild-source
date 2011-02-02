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
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.layout.PortletLayout;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.utils.UserUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.ActivitySchema;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;

public class CompiledFormServlet extends HttpServlet {

    private static final String TYPE_CARD = "card";
    private static final String TYPE_ADVANCEPROCESS = "advance";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOperation operations = new ServletOperation();
        operations.emptySession(request);
        HttpSession session = request.getSession();
        SOAPClient client = operations.getClient(session);
        Log.PORTLET.debug("Creating compiled form with following parameters");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        CardUtils utils = new CardUtils();
        CardConfiguration cardConfig = utils.getCardConfiguration(request);
        String email = StringUtils.defaultIfEmpty((String) session.getAttribute(UserUtils.EMAIL), "");
        Log.PORTLET.debug("Connected user email: " + email);
        StringBuilder layout = new StringBuilder();
        PortletLayout portletLayout = new PortletLayout(client, email, request.getContextPath());
        FormSerializer form = new FormSerializer(request.getContextPath());
        ProcessOperation operation = new ProcessOperation(client);
        CardOperation cardoperation = new CardOperation(client);
        Card card = cardoperation.getCard(cardConfig.getClassname(), cardConfig.getId());
        if (TYPE_CARD.equals(cardConfig.getType())) {
            layout.append(portletLayout.generateTitle(cardConfig.getClassdescription()));
            layout.append(form.generateCompiledCardLayout(client, cardConfig, false));
        } else if (TYPE_ADVANCEPROCESS.equals(cardConfig.getType())) {
            ActivitySchema activity = operation.getActivity(cardConfig.getClassname(), cardConfig.getId());
            layout = form.generateProcessLayout(activity, request, true);
        }
        layout.append(javascriptServerVars(card.getAttributeList()));
        out.write(layout.toString());
        out.flush();
        out.close();
    }


    private String javascriptServerVars(List<Attribute> cardAttributes) {
        StringBuilder serverVars = new StringBuilder();
        serverVars.append("<script>");
        serverVars.append("var CMDBuildServerVars = { ");
        int length = cardAttributes.size();
        for (int i=0; i<length; ++i) {
            Attribute attribute = cardAttributes.get(i);
            //if is reference or lookup use code
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
