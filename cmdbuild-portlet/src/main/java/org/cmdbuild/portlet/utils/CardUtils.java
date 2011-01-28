package org.cmdbuild.portlet.utils;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.layout.widget.WWType;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;

public class CardUtils {

    public CardConfiguration getCardConfiguration(HttpServletRequest request) {

        CardConfiguration configuration = new CardConfiguration();
        configuration.setId(Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("cardid"), "0")));
        configuration.setClassname(request.getParameter("classname"));
        configuration.setClassdescription(StringUtils.defaultIfEmpty(request.getParameter("classdescription"), ""));
        configuration.setPrivilege(request.getParameter(StringUtils.defaultIfEmpty("privilege", "")));
        configuration.setType(StringUtils.defaultIfEmpty(request.getParameter("type"), "process"));
        configuration.setFlowstatus(StringUtils.defaultIfEmpty(request.getParameter("flowstatus"), "open.running"));
        if (request.getSession().getAttribute("displayWorkflowNotes") != null) {
            configuration.setDisplayNotes((Boolean) request.getSession().getAttribute("displayWorkflowNotes"));
        } else {
            configuration.setDisplayNotes(false);
        }
        return configuration;
    }

    public List<WorkflowWidget> workflowWidgetsFromDefinition(WorkflowWidgetDefinition[] widgets) {
        List<WorkflowWidget> wwList = new ArrayList<WorkflowWidget>();
        if (widgets != null){
            for (WorkflowWidgetDefinition widget : widgets) {
                wwList.add(WWType.create(widget));
            }
        }
        return wwList;
    }

    public boolean isWritable(String privilege) {
        if ("write".equalsIgnoreCase(privilege)) {
            return true;
        } else {
            return false;
        }
    }
}
