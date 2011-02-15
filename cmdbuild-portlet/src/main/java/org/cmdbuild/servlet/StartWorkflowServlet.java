package org.cmdbuild.servlet;

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
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;

public class StartWorkflowServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		final Writer writer = response.getWriter();
		startWorkflow(request, writer);
		writer.flush();
		writer.close();
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException, FileNotFoundException, RemoteException {
		doPost(request, response);
	}

	private void startWorkflow(final HttpServletRequest request, final Writer writer) {
		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(request.getSession());
		final ProcessOperation op = new ProcessOperation(client);
		final Card workflow = operations.prepareWorkflow(RequestParams.create(request));
		final List<WorkflowWidgetSubmission> submissions = WorkflowWidget.getWorkflowWidgetSubmissions(request
				.getSession());
		try {
			int ret;
			try {
				ret = op.updateWorkflow(workflow, submissions);
				if (ret > 0) {
					for (final WorkflowWidget widget : WorkflowWidget.getWorkflowWidgetListFromSession(request
							.getSession())) {
						widget.extraWorkflowWidgetUpdate(request, ret);
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
			Log.PORTLET.warn("IO Exception while starting workflow", ex);
		}
	}
}
