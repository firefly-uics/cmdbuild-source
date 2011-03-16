package org.cmdbuild.portlet.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.Types;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.layout.widget.WWType;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.ReportOperation;
import org.cmdbuild.portlet.operation.ServletOperation;
import org.cmdbuild.portlet.operation.WSOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.ActivitySchema;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.ReportParams;
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.servlet.util.SessionAttributes;

public class FormSerializer {

	private final String contextPath;

	public FormSerializer(final String contextPath) {
		this.contextPath = contextPath;
	}

	public String processFormLayoutHeader(final boolean advance) {
		final StringBuffer layout = new StringBuffer();
		String advanceString = "";
		if (advance) {
			advanceString = "Advance";
		}
		layout.append("<div id=\"CMDBuild").append(advanceString).append(
				"ProcessFormPanel\" class=\"CMDBuildProcessContainer\" >");
		layout.append("<div id=\"CMDBuildHiddenLookupStore\" style=\"display: none\"></div>");
		layout.append("<form id=\"CMDBuild").append(advanceString).append(
				"Processform\" class=\"CMDBuilProcessForm\" enctype=\"multipart/form-data\">");
		layout.append("<div id=\"CMDBuild").append(advanceString).append("ProcessFormContainer\">");

		return layout.toString();
	}

	public String formLayoutFooter() {
		final StringBuffer layout = new StringBuffer();
		layout.append("</div>");
		layout.append("</form>");
		layout.append("</div>");
		return layout.toString();
	}

	private String generateProcessHelp(final boolean advance) {
		final StringBuffer layout = new StringBuffer();
		String advanceString = "";
		if (advance) {
			advanceString = "Advance";
		}
		layout.append("<div id=\"CMDBuild").append(advanceString).append(
				"ProcessHelp\" class=\"CMDBuildProcessContainer\"></div>");
		return layout.toString();
	}

	public StringBuilder generateReportLayout(final HttpServletRequest request, final List<AttributeSchema> parameters,
			final List<ReportParams> reportParameters) {
		final String useremail = StringUtils.defaultIfEmpty((String) request.getSession().getAttribute(
				SessionAttributes.EMAIL), StringUtils.EMPTY);
		final StringBuilder layout = new StringBuilder("");
		layout.append(addHiddenReportFields(request));
		if (parameters != null) {
			final ServletOperation operations = new ServletOperation();
			final SOAPClient client = operations.getClient(request.getSession());
			final PortletLayout portletlayout = new PortletLayout(client, useremail, contextPath);
			if (reportParameters != null && reportParameters.size() > 0) {
				for (final AttributeSchema as : parameters) {
					for (final ReportParams param : reportParameters) {
						if (param.getKey().equals(as.getDescription())) {
							layout.append(portletlayout.getComponent("Report", as, "", param.getValue(), true) + "\n");
						}
					}
				}
			} else {
				for (final AttributeSchema as : parameters) {
					layout.append(portletlayout.getComponent("Report", as, "", "", true) + "\n");
				}
			}
		}
		final ButtonLayout buttonLayout = new ButtonLayout();
		layout.append(buttonLayout.generateButtons(Types.Buttons.REPORT_FORM, null, true));
		return layout;
	}

	public StringBuilder generateCannotStartForm() {
		final StringBuilder result = new StringBuilder();
		result.append("Impossibile avviare il processo.");
		return result;
	}

	private String addHiddenReportFields(final HttpServletRequest request) {
		final StringBuilder layout = new StringBuilder();
		if (request.getParameter(ReportOperation.ID) != null) {
			layout.append(String.format("<input type=\"hidden\" name=\"%s\" value=\"%s\" />", ReportOperation.ID,
					request.getParameter(ReportOperation.ID)));
		}
		if (request.getParameter(ReportOperation.REPORT_NAME) != null) {
			layout.append(String.format("<input type=\"hidden\" name=\"%s\" value=\"%s\" />",
					ReportOperation.REPORT_NAME, request.getParameter(ReportOperation.REPORT_NAME)));
		}
		if (request.getParameter(ReportOperation.EXTENSION) != null) {
			layout.append(String.format("<input type=\"hidden\" name=\"%s\" value=\"%s\" />",
					ReportOperation.EXTENSION, request.getParameter(ReportOperation.EXTENSION)));
		}
		return layout.toString();
	}

	public StringBuilder generateEmptyCardLayout(final SOAPClient client, final CardConfiguration config,
			final String classname, final boolean readonly) {
		final PortletLayout layout = new PortletLayout(client, "", contextPath);
		final WSOperation operation = new WSOperation(client);
		final List<AttributeSchema> schema = operation.getAttributeList(classname);
		final StringBuilder result = new StringBuilder();
		if (schema != null) {
			for (final AttributeSchema as : schema) {
				result.append(layout.getComponent(classname, as, "", "", !readonly) + "\n");
			}
		}
		final ButtonLayout buttonLayout = new ButtonLayout();
		result.append(buttonLayout.generateButtons(Types.Buttons.EMPTY_CARD, config, true));
		return result;
	}

	public StringBuilder generateCompiledCardLayout(final SOAPClient client, final CardConfiguration config,
			final boolean readonly) {
		final PortletLayout layout = new PortletLayout(client, "", contextPath);
		final CardOperation operation = new CardOperation(client);
		final Card card = operation.getCard(config.getClassname(), config.getId());
		final List<AttributeSchema> schema = operation.getAttributeList(config.getClassname());
		final StringBuilder result = new StringBuilder();
		final FieldUtils utils = new FieldUtils();
		if (schema != null) {
			for (final AttributeSchema as : schema) {
				for (final Attribute attribute : card.getAttributeList()) {
					if (as.getName().equals(attribute.getName())) {
						final String visibility = as.getVisibility();
						if ("process".equals(config.getType()) || "advance".equals(config.getType())) {
							final boolean editableAndVisibile = utils.checkIsEditable(card, config.getType())
									&& utils.checkVisibility(visibility) && !readonly;
							result.append(layout.getComponent(config.getClassname(), as, attribute.getCode(), attribute
									.getValue(), editableAndVisibile)
									+ "\n");
						} else if ("card".equals(config.getType())) {
							result.append(layout.getComponent(config.getClassname(), as, attribute.getCode(), attribute
									.getValue(), utils.checkIsEditabileByCurrentUser(card.getMetadata()) && !readonly)
									+ "\n");
						}

					}
				}
			}
		}
		final ButtonLayout buttonLayout = new ButtonLayout();
		result.append(buttonLayout.generateButtons(Types.Buttons.EMPTY_CARD, config, true));
		return result;
	}

	private String addHiddenFieldId(final int id) {
		return "<input type=\"hidden\" name=\"id\" value=\"" + String.valueOf(id) + "\"/>";
	}

	public StringBuilder generateProcessLayout(final ActivitySchema activity, final HttpServletRequest request,
			final boolean compiled) {
		final StringBuilder result = new StringBuilder();
		final ServletOperation operations = new ServletOperation();
		final SOAPClient client = operations.getClient(request.getSession());
		final PortletLayout portletLayout = new PortletLayout(client, (String) request.getSession().getAttribute(
				"useremail"), contextPath);
		final CardUtils utils = new CardUtils();
		final CardConfiguration cardConfig = utils.getCardConfiguration(request);
		result.append(generateProcessForm(activity, cardConfig, portletLayout, compiled));
		result.append(generateWorkflowWidget(activity.getWidgets(), request));
		result.append(generateProcessHelp(compiled));
		return result;
	}

	private void createAndStoreWorkflowWidget(final WorkflowWidgetDefinition wwDef,
			final Map<String, WorkflowWidget> workflowWidgetMap) {
		final WorkflowWidget widget = WWType.create(wwDef);
		workflowWidgetMap.put(wwDef.getIdentifier(), widget);
	}

	private String createCompiledWorkflowForm(final ActivitySchema activity, final CardConfiguration config,
			final PortletLayout portletLayout) {
		final StringBuilder layout = new StringBuilder();
		final SOAPClient client = portletLayout.getClient();
		final CardOperation operation = new CardOperation(client);
		final Card card = operation.getCard(config.getClassname(), config.getId());
		layout.append(addHiddenFieldId(config.getId()));
		final List<AttributeSchema> schema = activity.getAttributes();
		final FieldUtils utils = new FieldUtils();
		if (schema != null) {
			for (final AttributeSchema as : schema) {
				for (final Attribute attribute : card.getAttributeList()) {
					if (as.getName().equals(attribute.getName())) {
						final String visibility = as.getVisibility();
						final boolean editableAndVisibile = utils.checkIsEditable(card, config.getType())
								&& utils.checkVisibility(visibility);
						layout.append(portletLayout.getComponent(config.getClassname(), as, attribute.getCode(),
								attribute.getValue(), editableAndVisibile)
								+ "\n");
					}
				}
			}
		}
		return layout.toString();
	}

	private String createEmptyWorkflowForm(final ActivitySchema activity, final CardConfiguration cardConfig,
			final PortletLayout portletLayout) {
		final StringBuilder layout = new StringBuilder();
		final CardUtils utils = new CardUtils();
		boolean editable = true;
		for (final AttributeSchema schema : activity.getAttributes()) {
			if (schema.getVisibility().equalsIgnoreCase("update")
					|| schema.getVisibility().equalsIgnoreCase("required")) {
				editable = true;
			} else {
				editable = false;
			}
			editable = utils.isWritable(cardConfig.getPrivilege());
			layout.append(portletLayout.getComponent(cardConfig.getClassname(), schema, "", "", editable) + "\n");
		}

		return layout.toString();
	}

	private String generateWorkflowWidget(final List<WorkflowWidgetDefinition> widgets, final HttpServletRequest request) {
		final StringBuilder layout = new StringBuilder();
		boolean addNotesWidget = false;
		boolean addAttachmentWidget = false;
		final Map<String, WorkflowWidget> workflowWidgetMap = new HashMap<String, WorkflowWidget>();
		if (widgets != null) {
			for (final WorkflowWidgetDefinition wwDef : widgets) {
				try {
					addNotesWidget |= isNotesWidget(wwDef);
					addAttachmentWidget |= isAttachmentWidget(wwDef);
					createAndStoreWorkflowWidget(wwDef, workflowWidgetMap);
				} catch (final Exception ex) {
					Log.PORTLET.debug("Unrecognised workflow widget " + wwDef.getType());
				}
			}
		}
		if (!addNotesWidget && PortletConfiguration.getInstance().forceDisplayWorkflowNotes()) {
			createAndStoreWorkflowWidget(createDefaultNoteWidget(), workflowWidgetMap);
		}
		if (!addAttachmentWidget && PortletConfiguration.getInstance().forceDisplayWorkflowAttachments()) {
			createAndStoreWorkflowWidget(createDefaultAttachmentWidget(), workflowWidgetMap);
		}
		WorkflowWidget.setWorkflowWidgetMap(request.getSession(), workflowWidgetMap);

		for (final WorkflowWidget widget : workflowWidgetMap.values()) {
			layout.append(widget.generateHtml(request));
		}
		return layout.toString();
	}

	private WorkflowWidgetDefinition createDefaultNoteWidget() {
		return createDefaultWidget(WWType.openNotes, "Note");
	}

	private WorkflowWidgetDefinition createDefaultAttachmentWidget() {
		return createDefaultWidget(WWType.openAttachment, "Allegati");
	}

	private WorkflowWidgetDefinition createDefaultWidget(final WWType type, final String label) {
		final WorkflowWidgetDefinition wwDef = new WorkflowWidgetDefinition();
		wwDef.setIdentifier(generateIdentifier(type, label));
		wwDef.setType(type.name());
		final WorkflowWidgetDefinitionParameter wwPar = new WorkflowWidgetDefinitionParameter();
		wwPar.setKey(WorkflowWidget.WW_BUTTON_LABEL);
		wwPar.setValue(label);
		wwDef.getParameters().add(wwPar);
		return wwDef;
	}

	private String generateIdentifier(final WWType type, final String label) {
		final StringBuilder sb = new StringBuilder();
		sb.append(WorkflowWidget.WW_BUTTON_LABEL).append("=\"").append(label).append("\"\n");
		return type.name() + sb.toString().hashCode();
	}

	private boolean isNotesWidget(final WorkflowWidgetDefinition w) {
		return "openNotes".equalsIgnoreCase(w.getType());
	}

	private boolean isAttachmentWidget(final WorkflowWidgetDefinition w) {
		return "openAttchment".equalsIgnoreCase(w.getType());
	}

	private String generateProcessForm(final ActivitySchema activity, final CardConfiguration cardConfig,
			final PortletLayout portletLayout, final boolean compiled) {
		final StringBuilder result = new StringBuilder();
		final FormSerializer form = new FormSerializer(contextPath);
		result.append(form.processFormLayoutHeader(compiled));
		if (compiled) {
			result.append(createCompiledWorkflowForm(activity, cardConfig, portletLayout));
		} else {
			result.append(createEmptyWorkflowForm(activity, cardConfig, portletLayout));
		}

		final ButtonLayout buttonLayout = new ButtonLayout();
		result.append(buttonLayout.generateButtons(Types.Buttons.EMPTY_CARD, cardConfig, true));
		result.append(form.formLayoutFooter());
		return result.toString();
	}
}
