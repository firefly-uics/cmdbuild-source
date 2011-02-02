package org.cmdbuild.portlet.layout.widget;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.ReportOperation;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.ReportParams;

public class CreateReportWidget extends WorkflowWidget {

    private enum Params {

        ReportType {

            public void handleParam(CreateReportWidget w, String value) {
                w.reportType = value;
            }
        },
        ReportCode {

            public void handleParam(CreateReportWidget w, String value) {
                w.reportCode = value;
            }
        },
        Id {

            public void handleParam(CreateReportWidget w, String value) {
                w.id = Integer.valueOf(value);
            }
        },
        StoreInProcess {

            public void handleParam(CreateReportWidget w, String value) {
                w.storeInProcess = Boolean.valueOf(value);
            }
        },
        forceextension {

            public void handleParam(CreateReportWidget w, String value) {
                w.extension = value;
            }
        },;

        abstract public void handleParam(CreateReportWidget w, String value);
    }
    private String reportType;
    private boolean storeInProcess;
    private String reportCode;
    private String extension;
    private int id;
    private List<ReportParams> reportParameters = new ArrayList<ReportParams>();

    public CreateReportWidget(WorkflowWidgetDefinition definition) {
        super(definition);
        for (WorkflowWidgetDefinitionParameter parameter : definition.getParameters()) {
            try {
                Params currentParam = Params.valueOf(parameter.getKey());
                currentParam.handleParam(this, parameter.getValue());
            } catch (Exception e) {
                reportParameters.add(convertToReportParams(parameter));
            }
        }
    }

    private ReportParams convertToReportParams(WorkflowWidgetDefinitionParameter parameter) {
        ReportParams param = new ReportParams();
        param.setKey(parameter.getKey());
        param.setValue(parameter.getValue());
        return param;
    }

    @Override
    public String generateHtml(HttpServletRequest request) {
        ServletOperation operations = new ServletOperation();
        operations.emptySession(request);
        SOAPClient client = operations.getClient(request.getSession());
        ReportOperation operation = new ReportOperation(client);
        List<AttributeSchema> parameters = operation.getReportParameters(id, extension);
        StringBuffer layout = new StringBuffer();
        layout.append("<div id=\"CMDBuildReportFormPanel\" class=\"CMDBuildProcessContainer\" >");
        layout.append("<form enctype=\"multipart/form-data\" class=\"CMDBuildReportWidgetForm\">");
        FormSerializer formLayout = new FormSerializer(request.getContextPath());
        layout.append(formLayout.generateReportLayout(request, parameters, reportParameters).toString());
        layout.append(generateHiddenFields());
        appendHiddenIdentifier(layout);
        layout.append("</form>");
        layout.append("</div>");
        return layout.toString();
    }

    private String generateHiddenFields() {
        StringBuilder layout = new StringBuilder();
        layout.append(String.format("<input type=\"hidden\" name=\"id\" value=\"%s\" />", id));
        layout.append(String.format("<input type=\"hidden\" name=\"extension\" value=\"%s\" />", extension));
        layout.append(String.format("<input type=\"hidden\" name=\"reportname\" value=\"%s\" />", reportCode));
        return layout.toString();
    }

    @Override
    public void manageWidgetSubmission(HttpServletRequest request, RequestParams params) {
    }
}
