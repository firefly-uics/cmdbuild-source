package org.cmdbuild.portlet;
import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import javax.portlet.PortletRequestDispatcher;

public class CMDBuildPortlet extends GenericPortlet {

    @Override
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        Log.PORTLET.debug("Portlet context path: "+request.getContextPath());
        String jsppage = request.getParameter("jspPage");
        PortletRequestDispatcher dispatcher = null;
        if (jsppage != null) {
            try {
                dispatcher = getPortletContext().getRequestDispatcher("/"+jsppage);
            } catch (Exception e) {
                dispatcher = getPortletContext().getRequestDispatcher("/view.jsp");
            }
        } else {
            dispatcher = getPortletContext().getRequestDispatcher("/view.jsp");
        }
        dispatcher.include(request, response);
    }
    @Override
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException { 
        PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/help.jsp");
        dispatcher.include(request, response);
    }
}