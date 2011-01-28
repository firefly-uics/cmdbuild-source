package org.cmdbuild.portlet.operation;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.AttachmentConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.exception.ConfigurationExcepion.ConfigurationExcepionType;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.servlet.ReportServlet;
import org.cmdbuild.servlet.WorkflowWidgetServlet;

/**
 *
 * @author Giuseppe Gortan
 */
public class ServletOperation {

    public void emptySession(HttpServletRequest request) {
        //Clear all workflow widget
        if (WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession()).size() > 0) {
            WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession()).clear();
        }

        //Clear all attachemnts
        if (WorkflowWidgetServlet.getCurrentAttachmentObject(request).size() > 0) {
            WorkflowWidgetServlet.getCurrentAttachmentObject(request).clear();
        }

        //Clear all report
        if (ReportServlet.REPORT_PARAMS.size() > 0) {
            ReportServlet.REPORT_PARAMS.clear();
        }

    }

    public Card prepareCard(HttpServletRequest request) {
        Card card = new Card();
        Log.PORTLET.debug("Preparing card ...");
        List<Attribute> attributes = new LinkedList<Attribute>();
        Enumeration parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String name = (String) parameters.nextElement();
            String value = request.getParameter(name);
            if (isIdentifier(name)) {
                continue;
            }
            Log.PORTLET.debug("Setting attribute " + name + " with value " + value);
            if (name.equalsIgnoreCase("classname")) {
                card.setClassName(value);
            } else if (name.equalsIgnoreCase("id")) {
                card.setId(Integer.parseInt(value));
            } else {
                Attribute attribute = new Attribute();
                attribute.setName(name);
                attribute.setValue(value);
                card.getAttributeList().add(attribute);
            }
        }
        return card;
    }

    public Card prepareWorkflow(RequestParams params) {
        Card card = new Card();
        List<Attribute> attributes = new LinkedList<Attribute>();
        Enumeration parameters = params.getParameterNames();
        Log.PORTLET.debug("Preparing card for workflow...");
        while (parameters.hasMoreElements()) {
            String name = (String) parameters.nextElement();
            String value = params.getParameter(name);
            Log.PORTLET.debug("Setting attribute " + name + " with value " + value);
            if (value != null && !value.equals("")) {
                prepareCardAttribute(card, name, value);
            } else {
                continue;
            }
            if (name.contains("attachment") || name.contains("dms")) {
                continue;
            }
        }
        return card;
    }

    private void prepareCardAttribute(Card card, String name, String value) {
        if (name.equalsIgnoreCase("classname")) {
            card.setClassName(value);
        } else if (name.equalsIgnoreCase("id")) {
            card.setId(Integer.valueOf(value));
        } else {
            Attribute attribute = new Attribute();
            attribute.setName(name);
            attribute.setValue(value);
            card.getAttributeList().add(attribute);
        }
    }

    public SOAPClient getClient(HttpSession session) {
        String username = (String) session.getAttribute("connectedUser");
        Log.PORTLET.debug("Connected user " + username);
        String trustedService = PortletConfiguration.getInstance().getTrustedService();
        String password = PortletConfiguration.getInstance().getServicePassword();
        String cmdbuildUrl = PortletConfiguration.getInstance().getCmdbuildUrl();
        String group = PortletConfiguration.getInstance().getServiceGroup();
        String user = trustedService + "#" + username + "@" + group;
        if (cmdbuildUrl != null && user != null && password != null) {
            SOAPClient client = new SOAPClient(cmdbuildUrl, user, password);
            return client;
        } else {
            throw ConfigurationExcepionType.PARAMETER_NOT_FOUND.createException();
        }
    }

    public String uploadPackage(HttpServletRequest request, AttachmentOperation operation, String classname, int id) {
        List<AttachmentConfiguration> attachments = WorkflowWidgetServlet.getCurrentAttachmentObject(request);
        boolean uploaded = false;
        String result = "";
        for (AttachmentConfiguration attachment : attachments) {
            File file = attachment.getFile();
            try {
                uploaded = operation.uploadAttachment(classname, id, attachment.getCategory(), attachment.getDescription(), attachment.getFilename(), file);
            } catch (RemoteException ex) {
                result = "<p>Non è stato possibile caricare alcuni allegati</p>";
                Log.PORTLET.warn("Error calling upload webservice", ex);
            }
        }
        if (uploaded) {
            result = "<p>Operazione eseguita correttamente</p>";
        } else {
            result = "<p>Il processo è stato avviato ma non è stato possibile caricare alcuni allegati</p>";
        }
        return result;
    }

    public boolean isIdentifier(String name) {
        return name.equals("CMDBuildIdentifier");
    }
}
