package org.cmdbuild.portlet.layout.widget;

import javax.servlet.http.HttpServletRequest;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetSubmission;


public class OpenNotesWidget extends WorkflowWidget {

    public OpenNotesWidget(WorkflowWidgetDefinition definition) {
        super(definition);
    }

    @Override
    public String generateHtml(HttpServletRequest request) {
        StringBuffer layout = new StringBuffer();
            layout.append("<div id=\"CMDBuildTexteditorcontainer\" class=\"CMDBuildProcessContainer\">");
                layout.append("<div>");
                    layout.append("<textarea id=\"CMDBuildTexteditor\"></textarea>");
                    layout.append("<input id=\"CMDBuildSaveeditor\" type=\"button\" value=\"Conferma\" onclick=\"CMDBuildSaveNote();\"/>");
                layout.append("</div>");
            layout.append("</div>");
        return layout.toString();
    }

    @Override
    public WorkflowWidgetSubmission createSubmissionObject() {
        return null;
    }
}
