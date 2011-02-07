package org.cmdbuild.portlet.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cmdbuild.portlet.configuration.ButtonBarConfiguration;
import org.cmdbuild.servlet.util.SessionAttributes;

public class ButtonUtils {

	public ButtonBarConfiguration generateButtonConfiguration(final HttpServletRequest request) {
		final ButtonBarConfiguration configuration = new ButtonBarConfiguration();
		final HttpSession session = request.getSession();
		configuration.setDisplayAttachment((Boolean) session
				.getAttribute(SessionAttributes.DISPLAY_WORKFLOW_ATTACHMENTS));
		configuration.setDisplayHelp((Boolean) session.getAttribute(SessionAttributes.DISPLAY_WORKFLOW_HELP));
		configuration.setDisplayNotes((Boolean) session.getAttribute(SessionAttributes.DISPLAY_WORKFLOW_NOTES));
		configuration.setDisplayWorkflowWidget((Boolean) session
				.getAttribute(SessionAttributes.DISPLAY_WORKFLOW_WIDGETS));
		return configuration;
	}

}
