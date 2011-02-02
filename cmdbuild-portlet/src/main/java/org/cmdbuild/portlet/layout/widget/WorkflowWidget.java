package org.cmdbuild.portlet.layout.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.operation.RequestParams;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;
import org.cmdbuild.servlet.WorkflowWidgetServlet;

public abstract class WorkflowWidget {

    private static final String WW_SESSION_OBJECT_NAME = "WorkflowWidgetMap";
    public static final String WW_BUTTON_LABEL = "ButtonLabel";
    protected final String buttonLabel;
    protected final String type;
    protected final String identifier;

    public WorkflowWidget(WorkflowWidgetDefinition definition) {
        if (definition != null) {
            for (WorkflowWidgetDefinitionParameter parameter : definition.getParameters()) {
                if (WW_BUTTON_LABEL.equals(parameter.getKey())) {
                    buttonLabel = parameter.getValue();
                    type = definition.getType();
                    identifier = definition.getIdentifier();
                    return;
                }
            }
        }

        buttonLabel = "";
        type = "";
        identifier = "";
    }

    public String getLabel() {
        return buttonLabel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return false;
    }

    public abstract String generateHtml(HttpServletRequest request);

    public static WorkflowWidget getWorkflowWidgetFromSession(HttpSession session, String identifier) {
        Map<String, WorkflowWidget> wwMap = (Map<String, WorkflowWidget>) session.getAttribute(WW_SESSION_OBJECT_NAME);
        return wwMap.get(identifier);
    }

    public static List<WorkflowWidget> getWorkflowWidgetListFromSession(HttpSession session) {
        Map<String, WorkflowWidget> wwMap = (Map<String, WorkflowWidget>) session.getAttribute(WW_SESSION_OBJECT_NAME);
        List<WorkflowWidget> wwList = new ArrayList<WorkflowWidget>();
        if (wwMap != null){
            for (WorkflowWidget ww : wwMap.values()) {
                wwList.add(ww);
            }
        }
        return wwList;
    }

    public static List<WorkflowWidgetSubmission> getWorkflowWidgetSubmissions(HttpSession session) {
        Map<String, WorkflowWidget> wwMap = (Map<String, WorkflowWidget>) session.getAttribute(WW_SESSION_OBJECT_NAME);
        List<WorkflowWidgetSubmission> wwSubList = new ArrayList<WorkflowWidgetSubmission>();
        if (wwMap != null) {
            for (WorkflowWidget ww : wwMap.values()) {
                WorkflowWidgetSubmission wwSub = ww.createSubmissionObject();
                if (wwSub != null) {
                    wwSubList.add(wwSub);
                }
            }
        }
        return wwSubList;
    }

    public static void setWorkflowWidgetMap(HttpSession session, Map<String, WorkflowWidget> workflowWidgetMap) {
        session.setAttribute(WW_SESSION_OBJECT_NAME, workflowWidgetMap);
    }

    public void manageWidgetSubmission(HttpServletRequest request, RequestParams params) {
    }

    protected WorkflowWidgetSubmission createSubmissionObject() {
        return null;
    };

    protected void appendHiddenIdentifier(StringBuffer layout) {
        layout.append("<input type=\"hidden\" name=\"").append(WorkflowWidgetServlet.WW_IDENTIFIER_PARAM).append("\" class=\"").append(WorkflowWidgetServlet.WW_IDENTIFIER_PARAM).append("\" value=\"").append(identifier).append("\" />");
    }

    public boolean extraWorkflowWidgetUpdate(HttpServletRequest request, int id) {
        return true;
    }

    public void cleanup(HttpServletRequest request) {
    }
}
