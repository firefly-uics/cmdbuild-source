package org.cmdbuild.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.ReportOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.ReportParams;

public class ReportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PARAMETERS_ACTION = "parameters";
	private static final String PRINT_REPORT_ACTION = "print";
	private static final String STORE_REPORT_PARAMETERS = "store";
	public static final List<ReportParams> REPORT_PARAMS = new ArrayList<ReportParams>();

	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(request.getSession());
		final String action = request.getParameter(ReportOperation.ACTION);
		if (PARAMETERS_ACTION.equals(action)) {
			final PrintWriter out = response.getWriter();
			operations.emptySession(request);
			response.setContentType("text/html;charset=UTF-8");
			out.write(generateParameterFormLayout(request, client));
			out.flush();
			out.close();
		} else if (PRINT_REPORT_ACTION.equals(action)) {
			final ReportOperation operation = new ReportOperation(client);
			operation.printReport(request, response);
		} else if (STORE_REPORT_PARAMETERS.equals(action)) {
			final ReportOperation operation = new ReportOperation(client);
			operation.serializeReportParams(request);
		}
	}

	private String generateParameterFormLayout(final HttpServletRequest request, final SOAPClient client) {
		final ReportOperation operation = new ReportOperation(client);
		final int id = Integer.valueOf(request.getParameter(ReportOperation.ID));
		final String extension = request.getParameter(ReportOperation.EXTENSION);
		final List<AttributeSchema> parameters = operation.getReportParameters(id, extension);
		if (parameters != null) {
			final StringBuffer layout = new StringBuffer();
			layout.append("<div id=\"CMDBuildReportFormPanel\" class=\"CMDBuildProcessContainer\" >");
			layout.append("<form id=\"CMDBuildReportform\" class=\"CMDBuilProcessForm\">");
			layout.append("<div id=\"CMDBuildReportFormContainer\">");
			final FormSerializer formLayout = new FormSerializer(request.getContextPath());
			layout.append(formLayout.generateReportLayout(request, parameters, null).toString());
			layout.append("</div>");
			layout.append("</form>");
			layout.append("</div>");
			return layout.toString();
		} else {
			return "";
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
