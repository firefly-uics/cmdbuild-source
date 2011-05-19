package org.cmdbuild.servlet;

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
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;

public class AdvanceWorkflowServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		final Writer writer = response.getWriter();
		AdvanceMultipartedWorkflow(request, writer);
		writer.flush();
		writer.close();
	}

	private void AdvanceMultipartedWorkflow(final HttpServletRequest request, final Writer writer) {
		try {
			final ServletOperation operations = new ServletOperation();
			final RequestParams params = RequestParams.create(request);
			final Card workflow = operations.prepareWorkflow(params);
			final SOAPClient client = operations.getClient(request.getSession());
			final ProcessOperation op = new ProcessOperation(client);
			final List<WorkflowWidgetSubmission> submissions = WorkflowWidget.getWorkflowWidgetSubmissions(request
					.getSession());
			request.getSession().removeAttribute(WorkflowWidgetServlet.WW_IDENTIFIER_PARAM);

			try {
				final int wfid = op.updateWorkflow(workflow, submissions);
				if (wfid > 0) {
					for (final WorkflowWidget widget : WorkflowWidget.getWorkflowWidgetListFromSession(request
							.getSession())) {
						widget.extraWorkflowWidgetUpdate(request, wfid);
						widget.cleanup(request);
					}
					writer.append("<p>Operazione eseguita correttamente</p>");
				} else {
					writer.append("<p>Si è verificato un errore avviando il processo.</p>");
				}
			} catch (final RemoteException ex) {
				writer.append("<p>Si è verificato un errore avviando il processo.</p>");
				Log.PORTLET.warn("Error calling webservice", ex);
			}
		} catch (final IOException ex) {
			Log.PORTLET.warn("IO Exception while advance workflow", ex);
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}
}
