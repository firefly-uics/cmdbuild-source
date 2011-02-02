package org.cmdbuild.portlet.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.operation.ServletOperation;

public class GridUtils {

    public GridConfiguration getGridConfiguration(HttpServletRequest request){
        ServletOperation servletOperations = new ServletOperation();
        GridConfiguration configuration = new GridConfiguration();
        HttpSession session = request.getSession();
        int page = Integer.valueOf(StringUtils.defaultIfEmpty(request.getParameter("page"), "1"));
        configuration.setPage(page);
        int maxResult = Integer.valueOf(StringUtils.defaultIfEmpty(request.getParameter("rp"), "10"));
        configuration.setMaxResult(maxResult);
        configuration.setDisplayDetailColumn((Boolean) session.getAttribute("displayDetailColumn"));
        configuration.setShowEmailColumn((Boolean) session.getAttribute("displayEmailColumn"));
        configuration.setAdvanceProcess((Boolean) session.getAttribute("advanceProcess"));
        configuration.setDisplayOnlyBaseDSP((Boolean) session.getAttribute("displayOnlyBaseDSP"));
        configuration.setDisplayHistory((Boolean) session.getAttribute("displayHistory"));
        configuration.setDisplayAttachmentList((Boolean) session.getAttribute("displayAttachmentList"));
        configuration.setQuery(StringUtils.defaultIfEmpty(request.getParameter("qtype"), ""));
        configuration.setFullTextQuery(StringUtils.defaultIfEmpty(request.getParameter("query"), ""));
        configuration.setClient(servletOperations.getClient(session));
        configuration.setStartIndex((page - 1) * maxResult);
        String sortname;
        String sortorder;
        if (request.getParameter("sortname")!= null &&request.getParameter("sortname").equals("undefined")) {
            sortname = PortletConfiguration.getInstance().getGridOrderColumn();
        } else {
            sortname = request.getParameter("sortname");
        }
        configuration.setSortname(StringUtils.defaultIfEmpty(sortname, ""));
        if (request.getParameter("sortorder")!=null && request.getParameter("sortorder").equals("undefined")) {
            sortorder = PortletConfiguration.getInstance().getGridOrderDirection();
        } else {
            sortorder = request.getParameter("sortorder");
        }
        configuration.setSortorder(StringUtils.defaultIfEmpty(sortorder, ""));
        return configuration;
    }

}
