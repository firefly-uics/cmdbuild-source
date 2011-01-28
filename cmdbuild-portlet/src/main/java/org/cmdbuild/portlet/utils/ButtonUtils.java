package org.cmdbuild.portlet.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.configuration.ButtonBarConfiguration;

public class ButtonUtils {

    public ButtonBarConfiguration generateButtonConfiguration(HttpServletRequest request){
        ButtonBarConfiguration configuration = new ButtonBarConfiguration();
        HttpSession session = request.getSession();
        configuration.setDisplayAttachment((Boolean) session.getAttribute("displayWorkflowAttachments"));
        configuration.setDisplayHelp((Boolean) session.getAttribute("displayWorkflowHelp"));
        configuration.setDisplayNotes((Boolean) session.getAttribute("displayWorkflowNotes"));
        configuration.setDisplayWorkflowWidget((Boolean) session.getAttribute("displayWorkflowWidgets"));
        return configuration;
    }

}
