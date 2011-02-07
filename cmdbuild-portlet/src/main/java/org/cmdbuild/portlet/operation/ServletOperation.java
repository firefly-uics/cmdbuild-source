package org.cmdbuild.portlet.operation;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.auth.AuthMethod;
import org.cmdbuild.portlet.configuration.AttachmentConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.exception.ConfigurationExcepion.ConfigurationExcepionType;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.servlet.ReportServlet;
import org.cmdbuild.servlet.WorkflowWidgetServlet;
import org.cmdbuild.servlet.util.SessionAttributes;
import org.cmdbuild.servlet.util.SessionUtils;

/**
 * 
 * @author Giuseppe Gortan
 */
public class ServletOperation {

	public void emptySession(final HttpServletRequest request) {
		// Clear all workflow widget
		if (WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession()).size() > 0) {
			WorkflowWidget.getWorkflowWidgetListFromSession(request.getSession()).clear();
		}

		// Clear all attachemnts
		if (WorkflowWidgetServlet.getCurrentAttachmentObject(request).size() > 0) {
			WorkflowWidgetServlet.getCurrentAttachmentObject(request).clear();
		}

		// Clear all report
		if (ReportServlet.REPORT_PARAMS.size() > 0) {
			ReportServlet.REPORT_PARAMS.clear();
		}

	}

	public Card prepareCard(final HttpServletRequest request) {
		final Card card = new Card();
		Log.PORTLET.debug("Preparing card ...");
		final Enumeration parameters = request.getParameterNames();
		while (parameters.hasMoreElements()) {
			final String name = (String) parameters.nextElement();
			final String value = request.getParameter(name);
			if (isIdentifier(name)) {
				continue;
			}
			Log.PORTLET.debug("Setting attribute " + name + " with value " + value);
			if (name.equalsIgnoreCase("classname")) {
				card.setClassName(value);
			} else if (name.equalsIgnoreCase("id")) {
				card.setId(Integer.parseInt(value));
			} else {
				final Attribute attribute = new Attribute();
				attribute.setName(name);
				attribute.setValue(value);
				card.getAttributeList().add(attribute);
			}
		}
		return card;
	}

	public Card prepareWorkflow(final RequestParams params) {
		final Card card = new Card();
		final Enumeration parameters = params.getParameterNames();
		Log.PORTLET.debug("Preparing card for workflow...");
		while (parameters.hasMoreElements()) {
			final String name = (String) parameters.nextElement();
			final String value = params.getParameter(name);
			Log.PORTLET.debug("Setting attribute " + name + " with value " + value);
			if (value != null && StringUtils.isNotEmpty(value)) {
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

	private void prepareCardAttribute(final Card card, final String name, final String value) {
		if (name.equalsIgnoreCase("classname")) {
			card.setClassName(value);
		} else if (name.equalsIgnoreCase("id")) {
			card.setId(Integer.valueOf(value));
		} else {
			final Attribute attribute = new Attribute();
			attribute.setName(name);
			attribute.setValue(value);
			card.getAttributeList().add(attribute);
		}
	}

	public SOAPClient getClient(final HttpSession session) {
		final PortletConfiguration portletConfiguration = PortletConfiguration.getInstance();
		final String url = portletConfiguration.getCmdbuildUrl();
		if ((url == null) || url.isEmpty()) {
			throw ConfigurationExcepionType.PARAMETER_NOT_FOUND.createException("url");
		}
		final String serviceUser = portletConfiguration.getServiceUser();
		if ((serviceUser == null) || serviceUser.isEmpty()) {
			throw ConfigurationExcepionType.PARAMETER_NOT_FOUND.createException("service user");
		}
		final String servicePassword = portletConfiguration.getServicePassword();
		if ((servicePassword == null) || servicePassword.isEmpty()) {
			throw ConfigurationExcepionType.PARAMETER_NOT_FOUND.createException("service password");
		}
		final String serviceGroup = portletConfiguration.getServiceGroup();
		final AuthMethod method = portletConfiguration.getAuthMethod();
		final String authentication;
		switch (method) {
		case USERNAME:
			authentication = SessionUtils.getAttribute(session, SessionAttributes.USERNAME);
			break;
		case EMAIL:
			authentication = SessionUtils.getAttribute(session, SessionAttributes.EMAIL);
			break;
		default:
			authentication = null;
			break;
		}
		if ((authentication == null) || authentication.isEmpty()) {
			throw ConfigurationExcepionType.PARAMETER_NOT_FOUND.createException("authentication");
		}
		final String user = serviceUser + "#" + authentication + "@" + serviceGroup;
		final SOAPClient client = new SOAPClient(url, user, servicePassword);
		return client;
	}

	public String uploadPackage(final HttpServletRequest request, final AttachmentOperation operation,
			final String classname, final int id) {
		final List<AttachmentConfiguration> attachments = WorkflowWidgetServlet.getCurrentAttachmentObject(request);
		boolean uploaded = false;
		String result = StringUtils.EMPTY;
		for (final AttachmentConfiguration attachment : attachments) {
			final File file = attachment.getFile();
			try {
				uploaded = operation.uploadAttachment(classname, id, attachment.getCategory(), attachment
						.getDescription(), attachment.getFilename(), file);
			} catch (final RemoteException ex) {
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

	public boolean isIdentifier(final String name) {
		return name.equals("CMDBuildIdentifier");
	}

}
