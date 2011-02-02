package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
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

public class AdvanceWorkflowServlet extends HttpServlet {
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
                response.setContentType("text/html;charset=UTF-8");
        Writer writer = response.getWriter();
        AdvanceMultipartedWorkflow(request, writer);
        writer.flush();
        writer.close();
    }

    private void AdvanceMultipartedWorkflow(HttpServletRequest request, Writer writer) {
                try {
            ServletOperation operations = new ServletOperation();
            RequestParams params = RequestParams.create(request);
            Card workflow = operations.prepareWorkflow(params);
            SOAPClient client = operations.getClient(request.getSession());
            ProcessOperation op = new ProcessOperation(client);
            List<WorkflowWidgetSubmission> submissions = WorkflowWidget.getWorkflowWidgetSubmissions(request.getSession());
            request.getSession().removeAttribute(WorkflowWidgetServlet.WW_IDENTIFIER_PARAM);

            try {
                int wfid = op.updateWorkflow(workflow,submissions);
                if (wfid > 0) {
                    for (WorkflowWidget widget : WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession())) {
                        widget.extraWorkflowWidgetUpdate(request, wfid);
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
            Log.PORTLET.warn("IO Exception while advance workflow", ex);
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
