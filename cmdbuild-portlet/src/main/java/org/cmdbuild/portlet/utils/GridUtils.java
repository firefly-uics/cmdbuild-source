package org.cmdbuild.portlet.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.servlet.util.SessionAttributes;

public class GridUtils {

	public GridConfiguration getGridConfiguration(final HttpServletRequest request) {
		final ServletOperation servletOperations = new ServletOperation();
		final GridConfiguration configuration = new GridConfiguration();
		final HttpSession session = request.getSession();
		final int page = Integer.valueOf(StringUtils.defaultIfEmpty(request.getParameter("page"), "1"));
		configuration.setPage(page);
		final int maxResult = Integer.valueOf(StringUtils.defaultIfEmpty(request.getParameter("rp"), "10"));
		configuration.setMaxResult(maxResult);
		configuration.setDisplayDetailColumn((Boolean) session.getAttribute(SessionAttributes.DISPLAY_DETAIL_COLUMN));
		configuration.setShowEmailColumn((Boolean) session.getAttribute(SessionAttributes.DISPLAY_EMAIL_COLUMN));
		configuration.setAdvanceProcess((Boolean) session.getAttribute(SessionAttributes.ADVANCE_PROCESS));
		configuration.setDisplayOnlyBaseDSP((Boolean) session.getAttribute(SessionAttributes.DISPLAY_ONLY_BASE_DSP));
		configuration.setDisplayHistory((Boolean) session.getAttribute(SessionAttributes.DISPLAY_HISTORY));
		configuration.setDisplayAttachmentList((Boolean) session
				.getAttribute(SessionAttributes.DISPLAY_ATTACHMENTS_LIST));
		configuration.setQuery(StringUtils.defaultIfEmpty(request.getParameter("qtype"), StringUtils.EMPTY));
		configuration.setFullTextQuery(StringUtils.defaultIfEmpty(request.getParameter("query"), StringUtils.EMPTY));
		configuration.setClient(servletOperations.getClient(session));
		configuration.setStartIndex((page - 1) * maxResult);
		String sortname;
		String sortorder;
		if (request.getParameter("sortname") != null && request.getParameter("sortname").equals("undefined")) {
			sortname = PortletConfiguration.getInstance().getGridOrderColumn();
		} else {
			sortname = request.getParameter("sortname");
		}
		configuration.setSortname(StringUtils.defaultIfEmpty(sortname, StringUtils.EMPTY));
		if (request.getParameter("sortorder") != null && request.getParameter("sortorder").equals("undefined")) {
			sortorder = PortletConfiguration.getInstance().getGridOrderDirection();
		} else {
			sortorder = request.getParameter("sortorder");
		}
		configuration.setSortorder(StringUtils.defaultIfEmpty(sortorder, StringUtils.EMPTY));
		return configuration;
	}

}
