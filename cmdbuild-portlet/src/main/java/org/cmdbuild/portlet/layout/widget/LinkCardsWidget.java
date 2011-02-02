package org.cmdbuild.portlet.layout.widget;

import org.cmdbuild.portlet.configuration.LinkCardItem;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.WorkflowWidgetSubmissionParameter;

public class LinkCardsWidget extends WorkflowWidget {

    private enum Params {

        ClassName {

            public void handleParam(LinkCardsWidget w, String value) {
                w.classname = value;
            }
        },
        SingleSelect {

            public void handleParam(LinkCardsWidget w, String value) {
                w.singleSelect = Integer.parseInt(value);
            }
        },
        NoSelect {

            public void handleParam(LinkCardsWidget w, String value) {
                w.noSelect = Integer.parseInt(value);
            }
        },
        Required {

            public void handleParam(LinkCardsWidget w, String value) {
                w.required = Integer.valueOf(value);
            }
        },
        Filter {

            public void handleParam(LinkCardsWidget w, String value) {
                w.filter = value;
            }
        },
        DefaultSelection {
            public void handleParam(LinkCardsWidget w, String value) {
                w.defaultValues = value;
            }
        },
        outputName {

            public void handleParam(LinkCardsWidget w, String value) {
                w.outputName = value;
            }
        };

        abstract public void handleParam(LinkCardsWidget w, String value);
    }
    private String classname;
    private int singleSelect;
    private int noSelect;
    private int required;
    private String defaultValues;
    private String filter;
    private String outputName;
    private List<String> values = new ArrayList<String>();
    private static final String ACTION = "action";
    private static final String ID = "id";
    private static final String STOREINSESSION = "add";
    private static final String REMOVEFROMSESSION = "remove";

    public LinkCardsWidget(WorkflowWidgetDefinition definition) {
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
    public boolean isRequired() {
        if (required > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String generateHtml(HttpServletRequest request) {
        ServletOperation operations = new ServletOperation();
        operations.emptySession(request);
        StringBuffer layout = new StringBuffer();
        HttpSession session = request.getSession();
        LinkCardItem item = getLinkCardItem();
        session.setAttribute(identifier, item);
        layout.append("<div id=\"").append("form_" + identifier).append("\" class=\"CMDBuildProcessContainer\">");
        layout.append("<form enctype=\"multipart/form-data\" class=\"CMDBuildWorkflowWidgetForm\">");
        appendHiddenIdentifier(layout);
        layout.append("<div class=\"CMDBuildLinkCardGridContainer\">");
        layout.append("<table class=\"CMDBuildLinkCardGrid\"></table>");
        layout.append("<div id=\"item_").append(identifier).append("\"></div>");
        if (defaultValues != null) {
            layout.append("<div id=\"defaultSelection_").append(identifier).append("\" style=\"display: none\">").append(defaultValues).append("</div>");
        }
        if (filter!= null) {
            layout.append("<div id=\"filter_").append(identifier).append("\" style=\"display: none\">").append(filter).append("</div>");
        }
        layout.append("</div>");
        layout.append("</form>");
        layout.append("</div>");
        return layout.toString();
    }

    @Override
    public void manageWidgetSubmission(HttpServletRequest request, RequestParams params) {
        String action = request.getParameter(ACTION);
        if (action.equals(STOREINSESSION)) {
            Enumeration parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String paramName = parameters.nextElement().toString();
                if (paramName.equals(ID)) {
                    values.add(request.getParameter(ID));
                }
            }
        } else if (action.equals(REMOVEFROMSESSION)) {
            values.remove(request.getParameter(ID));
        }
    }

    @Override
    public WorkflowWidgetSubmission createSubmissionObject() {
        WorkflowWidgetSubmission submission = new WorkflowWidgetSubmission();
        submission.setIdentifier(identifier);
        WorkflowWidgetSubmissionParameter parameter = new WorkflowWidgetSubmissionParameter();
        parameter.setKey(outputName);
        for (String value : values) {
            parameter.getValues().add(value);
        }
        submission.getParameters().add(parameter);
        return submission;
    }

    private LinkCardItem getLinkCardItem() {
        LinkCardItem item = new LinkCardItem();
        item.setClassname(classname);
        item.setFilter(filter);
        item.setIdentifier(identifier);
        item.setLabel(buttonLabel);
        item.setNoSelect(noSelect);
        item.setRequired(required);
        item.setSingleSelect(singleSelect);
        return item;
    }
}
