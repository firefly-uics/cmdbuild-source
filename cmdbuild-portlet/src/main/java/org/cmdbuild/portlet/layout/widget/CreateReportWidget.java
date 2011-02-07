package org.cmdbuild.portlet.layout.widget;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.ReportOperation;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.ReportParams;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;

public class CreateReportWidget extends WorkflowWidget {

	private enum Params {

		ReportType {

			@Override
			public void handleParam(final CreateReportWidget w, final String value) {
				w.reportType = value;
			}
		},
		ReportCode {

			@Override
			public void handleParam(final CreateReportWidget w, final String value) {
				w.reportCode = value;
			}
		},
		Id {

			@Override
			public void handleParam(final CreateReportWidget w, final String value) {
				w.id = Integer.valueOf(value);
			}
		},
		StoreInProcess {

			@Override
			public void handleParam(final CreateReportWidget w, final String value) {
				w.storeInProcess = Boolean.valueOf(value);
			}
		},
		forceextension {

			@Override
			public void handleParam(final CreateReportWidget w, final String value) {
				w.extension = value;
			}
		},
		;

		abstract public void handleParam(CreateReportWidget w, String value);
	}

	private String reportType;
	private boolean storeInProcess;
	private String reportCode;
	private String extension;
	private int id;
	private final List<ReportParams> reportParameters = new ArrayList<ReportParams>();

	public CreateReportWidget(final WorkflowWidgetDefinition definition) {
		super(definition);
		for (final WorkflowWidgetDefinitionParameter parameter : definition.getParameters()) {
			try {
				final Params currentParam = Params.valueOf(parameter.getKey());
				currentParam.handleParam(this, parameter.getValue());
			} catch (final Exception e) {
				reportParameters.add(convertToReportParams(parameter));
			}
		}
	}

	private ReportParams convertToReportParams(final WorkflowWidgetDefinitionParameter parameter) {
		final ReportParams param = new ReportParams();
		param.setKey(parameter.getKey());
		param.setValue(parameter.getValue());
		return param;
	}

	@Override
	public String generateHtml(final HttpServletRequest request) {
		final ServletOperation operations = new ServletOperation();
		operations.emptySession(request);
		final SOAPClient client = operations.getClient(request.getSession());
		final ReportOperation operation = new ReportOperation(client);
		final List<AttributeSchema> parameters = operation.getReportParameters(id, extension);
		final StringBuffer layout = new StringBuffer();
		layout.append("<div id=\"CMDBuildReportFormPanel\" class=\"CMDBuildProcessContainer\" >");
		layout.append("<form enctype=\"multipart/form-data\" class=\"CMDBuildReportWidgetForm\">");
		final FormSerializer formLayout = new FormSerializer(request.getContextPath());
		layout.append(formLayout.generateReportLayout(request, parameters, reportParameters).toString());
		layout.append(generateHiddenFields());
		appendHiddenIdentifier(layout);
		layout.append("</form>");
		layout.append("</div>");
		return layout.toString();
	}

	private String generateHiddenFields() {
		final StringBuilder layout = new StringBuilder();
		layout.append(String.format("<input type=\"hidden\" name=\"id\" value=\"%s\" />", id));
		layout.append(String.format("<input type=\"hidden\" name=\"extension\" value=\"%s\" />", extension));
		layout.append(String.format("<input type=\"hidden\" name=\"reportname\" value=\"%s\" />", reportCode));
		return layout.toString();
	}

	@Override
	public void manageWidgetSubmission(final HttpServletRequest request, final RequestParams params) {
	}
}
