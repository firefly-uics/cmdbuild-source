package org.cmdbuild.portlet.layout.widget;

import javax.servlet.http.HttpServletRequest;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.layout.FormSerializer;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.WorkflowWidgetSubmissionParameter;

public class CreateModifyCardWidget extends WorkflowWidget {

    private enum Params {

        ClassName {

            public void handleParam(CreateModifyCardWidget w, String value) {
                w.classname = value;
            }
        },
        ReadOnly {

            public void handleParam(CreateModifyCardWidget w, String value) {
                w.readonly = Boolean.parseBoolean(value);
            }
        },
        Reference {

            public void handleParam(CreateModifyCardWidget w, String value) {
                w.reference = value;
            }
        },
        id {

            public void handleParam(CreateModifyCardWidget w, String value) {
                w.id = Integer.valueOf(value);
            }
        },
        outputName {

            public void handleParam(CreateModifyCardWidget w, String value) {
                w.outputName = value;
            }
        };

        abstract public void handleParam(CreateModifyCardWidget w, String value);
    }
    private String classname;
    private boolean readonly;
    private int cardid = -1;
    private int id;
    private String reference;
    private String outputName;

    public CreateModifyCardWidget(WorkflowWidgetDefinition definition) {
        super(definition);
        for (WorkflowWidgetDefinitionParameter parameter : definition.getParameters()) {
            try {
                Params currentParam = Params.valueOf(parameter.getKey());
                currentParam.handleParam(this, parameter.getValue());
            } catch (Exception e) {
            }
        }
    }

    @Override
    public String generateHtml(HttpServletRequest request) {
        ServletOperation operations = new ServletOperation();
        operations.emptySession(request);
        SOAPClient client = operations.getClient(request.getSession());
        StringBuffer layout = new StringBuffer();
        CardUtils utils = new CardUtils();
        CardConfiguration config = utils.getCardConfiguration(request);
        FormSerializer form = new FormSerializer(request.getContextPath());
        String formContent = "";
        if (!"".equals(reference) && id > 0) {
            config.setClassname(classname);
            config.setId(id);
            formContent = form.generateCompiledCardLayout(client, config, readonly).toString();
        } else {
            formContent = form.generateEmptyCardLayout(client, config, classname, readonly).toString();
        }
        layout.append("<div id=\"").append(classname).append("\" class=\"CMDBuildProcessContainer\">");
        layout.append("<form enctype=\"multipart/form-data\" class=\"CMDBuildWorkflowWidgetForm\">");
        appendHiddenIdentifier(layout);
        layout.append(formContent);
        layout.append("</form>");
        layout.append("</div>");
        return layout.toString();
    }

    @Override
    public void manageWidgetSubmission(HttpServletRequest request, RequestParams params) {
        ServletOperation operations = new ServletOperation();
        SOAPClient client = operations.getClient(request.getSession());
        Card card = operations.prepareCard(request);
        card.setClassName(classname);
        CardOperation operation = new CardOperation(client);
        cardid = operation.createCard(card);
    }

    @Override
    public WorkflowWidgetSubmission createSubmissionObject() {
        if (cardid > 0) {
            WorkflowWidgetSubmission submission = new WorkflowWidgetSubmission();
            submission.setIdentifier(identifier);
            WorkflowWidgetSubmissionParameter parameter = new WorkflowWidgetSubmissionParameter();
            parameter.setKey(outputName);
            parameter.getValues().add(String.valueOf(cardid));
            submission.getParameters().add(parameter);
            return submission;
        } else {
            return null;
        }
    }
}
