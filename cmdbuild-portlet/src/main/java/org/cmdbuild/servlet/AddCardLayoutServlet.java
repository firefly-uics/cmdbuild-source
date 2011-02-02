package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.layout.PortletLayout;
import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.ActivitySchema;

public class AddCardLayoutServlet extends HttpServlet {

    private static final String EMAIL = "useremail";
    private static final String TYPE_CARD = "card";
    private static final String TYPE_PROCESS = "process";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOperation operations = new ServletOperation();
        operations.emptySession(request);
        HttpSession session = request.getSession();
        SOAPClient client = operations.getClient(session);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String email = StringUtils.defaultIfEmpty((String) session.getAttribute(EMAIL), "");
        Log.PORTLET.debug("Connected user email: " + email);
        CardUtils utils = new CardUtils();
        CardConfiguration cardConfig = utils.getCardConfiguration(request);
        StringBuilder layout = new StringBuilder();
        PortletLayout portletLayout = new PortletLayout(client, email, request.getContextPath());
        if (TYPE_CARD.equals(cardConfig.getType())) {
            FormSerializer form = new FormSerializer(request.getContextPath());
            layout.append(portletLayout.generateTitle(cardConfig.getClassdescription()));
            layout = form.generateEmptyCardLayout(client, cardConfig, cardConfig.getClassname(), false);
        } else if (TYPE_PROCESS.equals(cardConfig.getType())) {
            ProcessOperation operation = new ProcessOperation(client);
            ActivitySchema activity = operation.getActivity(cardConfig.getClassname(), -1);
            FormSerializer form = new FormSerializer(request.getContextPath());
            if (activity != null) {
                layout = form.generateProcessLayout(activity, request, false);
            } else {
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
        StringBuilder serverVars = new StringBuilder();
        serverVars.append("<script>");
        serverVars.append("var CMDBuildServerVars = {};");
        serverVars.append("</script>");
        return serverVars.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
}
