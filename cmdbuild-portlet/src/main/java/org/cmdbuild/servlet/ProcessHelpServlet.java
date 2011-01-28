package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.ws.SOAPClient;

public class ProcessHelpServlet extends HttpServlet {

    private static final String CLASSNAME = "classname";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession(true);
        SOAPClient client = operations.getClient(session);
        ProcessOperation operation = new ProcessOperation(client);
        String classname = request.getParameter(CLASSNAME);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String helpstring = "<p>";
            String helpText = operation.getProcessHelp(classname);
            if (helpText != null && helpText.length() > 0){
                helpstring = helpstring + helpText + "</p>";

            } else {
                helpstring = helpstring + "Nessun aiuto disponibile</p>";
            }
            out.write(helpstring);
            out.flush();
        } finally {
            out.close();
        }
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
