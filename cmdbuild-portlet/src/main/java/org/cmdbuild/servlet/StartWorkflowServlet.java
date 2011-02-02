package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.operation.ProcessOperation;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;

public class StartWorkflowServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        Writer writer = response.getWriter();
        startWorkflow(request, writer);
        writer.flush();
        writer.close();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException, FileNotFoundException, RemoteException {
        doPost(request, response);
    }

    private void startWorkflow(HttpServletRequest request, Writer writer) {
        ServletOperation operations = new ServletOperation();
        SOAPClient client = operations.getClient(request.getSession());
        ProcessOperation op = new ProcessOperation(client);
        Card workflow = operations.prepareWorkflow(RequestParams.create(request));
        List<WorkflowWidgetSubmission> submissions = WorkflowWidget.getWorkflowWidgetSubmissions(request.getSession());
        try {
            int ret;
            try {
                ret = op.updateWorkflow(workflow, submissions);
                if (ret > 0) {
                    for (WorkflowWidget widget : WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession())) {
                        widget.extraWorkflowWidgetUpdate(request, ret);
                        widget.cleanup(request);
                    }
                    writer.append("<p>Operazione eseguita correttamente</p>");
                } else {
                    writer.append("<p>Si è verificato un errore avviando il processo.</p>");
                }
            } catch (RemoteException ex) {
                writer.append("<p>Si è verificato un errore avviando il processo.</p>");
                Log.PORTLET.warn("Error calling webservice", ex);
            }
        } catch (IOException ex) {
            Log.PORTLET.warn("IO Exception while starting workflow", ex);
        }
    }
}
