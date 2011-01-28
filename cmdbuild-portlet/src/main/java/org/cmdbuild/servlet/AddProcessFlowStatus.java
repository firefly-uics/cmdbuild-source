package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.layout.ComponentLayout;
import org.cmdbuild.portlet.layout.LookupComponentSerializer;
import org.cmdbuild.portlet.ws.SOAPClient;

public class AddProcessFlowStatus extends HttpServlet {

    private static final String CLASSNAME = "classname";
    private static final String FLOWSTATUS = "flowstatus";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession(true);
        SOAPClient client = operations.getClient(session);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Log.PORTLET.debug("Creating flow status html code");
        String classname = request.getParameter(CLASSNAME);
        Log.PORTLET.debug("- Classname: " + classname);
        String flowstatus = request.getParameter(FLOWSTATUS);
        Log.PORTLET.debug("Displaying processes with id " + flowstatus);
        try {
            ComponentLayout layout = new ComponentLayout();
            layout.setClient(client);
            LookupComponentSerializer lookupLayout = new LookupComponentSerializer(layout);
            String statoprocesso = lookupLayout.addFlowStatusLookup(flowstatus, classname);
            out.write(statoprocesso);
            out.flush();
        } finally {
            out.close();
        }
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
