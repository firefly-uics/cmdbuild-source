package org.cmdbuild.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class CMDBuildPortlet extends GenericPortlet {

	@Override
	public void doView(final RenderRequest request, final RenderResponse response) throws PortletException, IOException {
		Log.PORTLET.debug("Portlet context path: " + request.getContextPath());
		final String jsppage = request.getParameter("jspPage");
		PortletRequestDispatcher dispatcher = null;
		if (jsppage != null) {
			try {
				dispatcher = getPortletContext().getRequestDispatcher("/" + jsppage);
			} catch (final Exception e) {
				dispatcher = getPortletContext().getRequestDispatcher("/view.jsp");
			}
		} else {
			dispatcher = getPortletContext().getRequestDispatcher("/view.jsp");
		}
		dispatcher.include(request, response);
	}

	@Override
	public void doHelp(final RenderRequest request, final RenderResponse response) throws PortletException, IOException {
		final PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/help.jsp");
		dispatcher.include(request, response);
	}
}