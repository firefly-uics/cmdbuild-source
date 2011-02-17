package org.cmdbuild.portlet.operation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.ReportParams;
import org.cmdbuild.servlet.ReportServlet;

public class ReportOperation extends WSOperation {

	public static final String ID = "id";
	public static final String EXTENSION = "extension";
	public static final String REPORT_NAME = "reportname";
	public static final String ACTION = "action";
	public static final String EMAIL = "useremail";

	public ReportOperation(final SOAPClient client) {
		super(client);
	}

	public void printReport(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final List<ReportParams> params = ReportServlet.REPORT_PARAMS;
		int id = 0;
		String extension = "";
		String name = "";
		if (params != null) {
			for (final ReportParams param : params) {
				if (ID.equals(param.getKey()) && param.getValue() != null) {
					id = Integer.valueOf(param.getValue());
				}
				if (EXTENSION.equals(param.getKey()) && param.getValue() != null) {
					extension = param.getValue();
				}
				if (REPORT_NAME.equals(param.getKey()) && param.getValue() != null) {
					name = param.getValue();
				}
			}
		} else {
			id = Integer.valueOf(request.getParameter(ID));
			extension = request.getParameter(EXTENSION);
			name = request.getParameter(REPORT_NAME);
		}
		final String filename = name + "." + extension;

		final DataHandler data = getReport(id, extension, params);
		// Clear report parameters list
		ReportServlet.REPORT_PARAMS.clear();
		response.setContentType(data.getContentType());
		response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\";", filename));
		response.setHeader("Expires", "0");
		data.writeTo(response.getOutputStream());
	}

	public void serializeReportParams(final HttpServletRequest request) {
		final Enumeration parameters = request.getParameterNames();
		while (parameters.hasMoreElements()) {
			final String name = (String) parameters.nextElement();
			final String value = request.getParameter(name);
			final ReportParams params = new ReportParams();
			params.setKey(name);
			params.setValue(value);
			ReportServlet.REPORT_PARAMS.add(params);
		}
	}

	public DataHandler getReport(final int id, final String extension, final List<ReportParams> params) {
		return getService().getReport(id, extension, params);
	}

	public List<AttributeSchema> getReportParameters(final int id, final String extension) {
		return getService().getReportParameters(id, extension);
	}
}
