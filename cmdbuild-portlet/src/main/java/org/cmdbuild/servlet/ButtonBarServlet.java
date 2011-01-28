package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.ButtonBarConfiguration;
import org.cmdbuild.portlet.layout.ButtonLayout;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.utils.ButtonUtils;

public class ButtonBarServlet extends HttpServlet {

    private static final String ADVANCE_PROCESS = "advanceProcess";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        boolean advance = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter(ADVANCE_PROCESS), "false"));
        ButtonUtils buttonUtils = new ButtonUtils();
        ButtonBarConfiguration buttonBarConfiguration = buttonUtils.generateButtonConfiguration(request);
        StringBuilder layout = new StringBuilder();
        ButtonLayout buttonLayout = new ButtonLayout();
        List<WorkflowWidget> wwList = WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession());
        layout.append(buttonLayout.generateProcessButtonBar(buttonBarConfiguration, wwList, advance));
        try {
            out.write(layout.toString());
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
