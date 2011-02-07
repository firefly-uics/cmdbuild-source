package org.cmdbuild.portlet.utils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.layout.widget.WWType;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.servlet.util.SessionAttributes;

public class CardUtils {

	public CardConfiguration getCardConfiguration(final HttpServletRequest request) {

		final CardConfiguration configuration = new CardConfiguration();
		configuration.setId(Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("cardid"), "0")));
		configuration.setClassname(request.getParameter("classname"));
		configuration.setClassdescription(StringUtils.defaultIfEmpty(request.getParameter("classdescription"),
				StringUtils.EMPTY));
		configuration.setPrivilege(request.getParameter(StringUtils.defaultIfEmpty("privilege", StringUtils.EMPTY)));
		configuration.setType(StringUtils.defaultIfEmpty(request.getParameter("type"), "process"));
		configuration.setFlowstatus(StringUtils.defaultIfEmpty(request.getParameter("flowstatus"), "open.running"));
		if (request.getSession().getAttribute(SessionAttributes.DISPLAY_WORKFLOW_NOTES) != null) {
			configuration.setDisplayNotes((Boolean) request.getSession().getAttribute(
					SessionAttributes.DISPLAY_WORKFLOW_NOTES));
		} else {
			configuration.setDisplayNotes(false);
		}
		return configuration;
	}

	public List<WorkflowWidget> workflowWidgetsFromDefinition(final WorkflowWidgetDefinition[] widgets) {
		final List<WorkflowWidget> wwList = new ArrayList<WorkflowWidget>();
		if (widgets != null) {
			for (final WorkflowWidgetDefinition widget : widgets) {
				wwList.add(WWType.create(widget));
			}
		}
		return wwList;
	}

	public boolean isWritable(final String privilege) {
		if ("write".equalsIgnoreCase(privilege)) {
			return true;
		} else {
			return false;
		}
	}
}
