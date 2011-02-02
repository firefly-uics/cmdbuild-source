package org.cmdbuild.portlet.layout;

import java.util.HashMap;
import java.util.LinkedList;
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
import org.cmdbuild.services.soap.WorkflowWidgetDefinition;
import org.cmdbuild.services.soap.WorkflowWidgetDefinitionParameter;
import org.cmdbuild.services.soap.ReportParams;

public class FormSerializer {

    private String contextPath;

    public FormSerializer(String contextPath) {
        this.contextPath = contextPath;
    }

    public String processFormLayoutHeader(boolean advance) {
        StringBuffer layout = new StringBuffer();
        String advanceString = "";
        if (advance) {
            advanceString = "Advance";
        }
        layout.append("<div id=\"CMDBuild").append(advanceString).append("ProcessFormPanel\" class=\"CMDBuildProcessContainer\" >");
        layout.append("<div id=\"CMDBuildHiddenLookupStore\" style=\"display: none\"></div>");
        layout.append("<form id=\"CMDBuild").append(advanceString).append("Processform\" class=\"CMDBuilProcessForm\" enctype=\"multipart/form-data\">");
        layout.append("<div id=\"CMDBuild").append(advanceString).append("ProcessFormContainer\">");

        return layout.toString();
    }

    public String formLayoutFooter() {
        StringBuffer layout = new StringBuffer();
        layout.append("</div>");
        layout.append("</form>");
        layout.append("</div>");
        return layout.toString();
    }

    private String generateProcessHelp(boolean advance) {
        StringBuffer layout = new StringBuffer();
        String advanceString = "";
        if (advance) {
            advanceString = "Advance";
        }
        layout.append("<div id=\"CMDBuild").append(advanceString).append("ProcessHelp\" class=\"CMDBuildProcessContainer\"></div>");
        return layout.toString();
    }

    public StringBuilder generateReportLayout(HttpServletRequest request, List<AttributeSchema> parameters, List<ReportParams> reportParameters) {
        String useremail = StringUtils.defaultIfEmpty((String) request.getSession().getAttribute(ReportOperation.EMAIL), "");
        StringBuilder layout = new StringBuilder("");
        layout.append(addHiddenReportFields(request));
        if (parameters != null) {
            ServletOperation operations = new ServletOperation();
            SOAPClient client = operations.getClient(request.getSession());
            PortletLayout portletlayout = new PortletLayout(client, useremail, contextPath);
            if (reportParameters != null && reportParameters.size() > 0) {
                for (AttributeSchema as : parameters) {
                    for (ReportParams param : reportParameters) {
                        if (param.getKey().equals(as.getDescription())) {
                            layout.append(portletlayout.getComponent("Report", as, "", param.getValue(), true) + "\n");
                        }
                    }
                }
            } else {
                for (AttributeSchema as : parameters) {
                    layout.append(portletlayout.getComponent("Report", as, "", "", true) + "\n");
                }
            }
        }
        ButtonLayout buttonLayout = new ButtonLayout();
        layout.append(buttonLayout.generateButtons(Types.Buttons.REPORT_FORM, null, true));
        return layout;
    }

    public StringBuilder generateCannotStartForm() {
        StringBuilder result = new StringBuilder();
        result.append("Non è possibile avviare il processo.");
        return result;
    }

    private String addHiddenReportFields(HttpServletRequest request) {
        StringBuilder layout = new StringBuilder();
        if (request.getParameter(ReportOperation.ID) != null) {
            layout.append(String.format("<input type=\"hidden\" name=\"%s\" value=\"%s\" />", ReportOperation.ID, request.getParameter(ReportOperation.ID)));
        }
        if (request.getParameter(ReportOperation.REPORT_NAME) != null) {
            layout.append(String.format("<input type=\"hidden\" name=\"%s\" value=\"%s\" />", ReportOperation.REPORT_NAME, request.getParameter(ReportOperation.REPORT_NAME)));
        }
        if (request.getParameter(ReportOperation.EXTENSION) != null) {
            layout.append(String.format("<input type=\"hidden\" name=\"%s\" value=\"%s\" />", ReportOperation.EXTENSION, request.getParameter(ReportOperation.EXTENSION)));
        }
        return layout.toString();
    }

    public StringBuilder generateEmptyCardLayout(SOAPClient client, CardConfiguration config, String classname, boolean readonly) {
        PortletLayout layout = new PortletLayout(client, "", contextPath);
        WSOperation operation = new WSOperation(client);
        List<AttributeSchema> schema = operation.getAttributeList(classname);
        StringBuilder result = new StringBuilder();
        if (schema != null) {
            for (AttributeSchema as : schema) {
                result.append(layout.getComponent(classname, as, "", "", !readonly) + "\n");
            }
        }
        ButtonLayout buttonLayout = new ButtonLayout();
        result.append(buttonLayout.generateButtons(Types.Buttons.EMPTY_CARD, config, true));
        return result;
    }

    public StringBuilder generateCompiledCardLayout(SOAPClient client, CardConfiguration config, boolean readonly) {
        PortletLayout layout = new PortletLayout(client, "", contextPath);
        CardOperation operation = new CardOperation(client);
        Card card = operation.getCard(config.getClassname(), config.getId());
        List<AttributeSchema> schema = operation.getAttributeList(config.getClassname());
        StringBuilder result = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        if (schema != null) {
            for (AttributeSchema as : schema) {
                for (Attribute attribute : card.getAttributeList()) {
                    if (as.getName().equals(attribute.getName())) {
                        String visibility = as.getVisibility();
                        if ("process".equals(config.getType()) || "advance".equals(config.getType())) {
                            boolean editableAndVisibile = utils.checkIsEditable(card, config.getType()) && utils.checkVisibility(visibility) && !readonly;
                            result.append(layout.getComponent(config.getClassname(), as, attribute.getCode(), attribute.getValue(), editableAndVisibile) + "\n");
                        } else if ("card".equals(config.getType())) {
                            result.append(layout.getComponent(config.getClassname(), as, attribute.getCode(), attribute.getValue(), utils.checkIsEditabileByCurrentUser(card.getMetadata()) && !readonly) + "\n");
                        }

                    }
                }
            }
        }
        ButtonLayout buttonLayout = new ButtonLayout();
        result.append(buttonLayout.generateButtons(Types.Buttons.EMPTY_CARD, config, true));
        return result;
    }

    private String addHiddenFieldId(int id) {
        return "<input type=\"hidden\" name=\"id\" value=\"" + String.valueOf(id) + "\"/>";
    }

    public StringBuilder generateProcessLayout(ActivitySchema activity, HttpServletRequest request, boolean compiled) {
        StringBuilder result = new StringBuilder();
        ServletOperation operations = new ServletOperation();
        SOAPClient client = operations.getClient(request.getSession());
        PortletLayout portletLayout = new PortletLayout(client, (String) request.getSession().getAttribute("useremail"), contextPath);
        CardUtils utils = new CardUtils();
        CardConfiguration cardConfig = utils.getCardConfiguration(request);
        result.append(generateProcessForm(activity, cardConfig, portletLayout, compiled));
        result.append(generateWorkflowWidget(activity.getWidgets(), request));
        result.append(generateProcessHelp(compiled));
        return result;
    }

    private void createAndStoreWorkflowWidget(WorkflowWidgetDefinition wwDef, Map<String, WorkflowWidget> workflowWidgetMap) {
        WorkflowWidget widget = WWType.create(wwDef);
        workflowWidgetMap.put(wwDef.getIdentifier(), widget);
    }

    private String createCompiledWorkflowForm(ActivitySchema activity, CardConfiguration config, PortletLayout portletLayout) {
        StringBuilder layout = new StringBuilder();
        SOAPClient client = portletLayout.getClient();
        CardOperation operation = new CardOperation(client);
        Card card = operation.getCard(config.getClassname(), config.getId());
        layout.append(addHiddenFieldId(config.getId()));
        List<AttributeSchema> schema = activity.getAttributes();
        FieldUtils utils = new FieldUtils();
        if (schema != null) {
            for (AttributeSchema as : schema) {
                for (Attribute attribute : card.getAttributeList()) {
                    if (as.getName().equals(attribute.getName())) {
                        String visibility = as.getVisibility();
                        boolean editableAndVisibile = utils.checkIsEditable(card, config.getType()) && utils.checkVisibility(visibility);
                        layout.append(portletLayout.getComponent(config.getClassname(), as, attribute.getCode(), attribute.getValue(), editableAndVisibile) + "\n");
                    }
                }
            }
        }
        return layout.toString();
    }

    private String createEmptyWorkflowForm(ActivitySchema activity, CardConfiguration cardConfig, PortletLayout portletLayout) {
        StringBuilder layout = new StringBuilder();
        CardUtils utils = new CardUtils();
        boolean editable = true;
        for (AttributeSchema schema : activity.getAttributes()) {
            if (schema.getVisibility().equalsIgnoreCase("update") || schema.getVisibility().equalsIgnoreCase("required")) {
                editable = true;
            } else {
                editable = false;
            }
            editable = utils.isWritable(cardConfig.getPrivilege());
            layout.append(portletLayout.getComponent(cardConfig.getClassname(), schema, "", "", editable) + "\n");
        }

        return layout.toString();
    }

    private String generateWorkflowWidget(List<WorkflowWidgetDefinition> widgets, HttpServletRequest request) {
        StringBuilder layout = new StringBuilder();
        boolean addNotesWidget = false;
        boolean addAttachmentWidget = false;
        Map<String, WorkflowWidget> workflowWidgetMap = new HashMap<String, WorkflowWidget>();
        if (widgets != null) {
            for (WorkflowWidgetDefinition wwDef : widgets) {
                try {
                    addNotesWidget |= isNotesWidget(wwDef);
                    addAttachmentWidget |= isAttachmentWidget(wwDef);
                    createAndStoreWorkflowWidget(wwDef, workflowWidgetMap);
                } catch (Exception ex) {
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

        for (WorkflowWidget widget : workflowWidgetMap.values()) {
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

    private WorkflowWidgetDefinition createDefaultWidget(WWType type, String label) {
        WorkflowWidgetDefinition wwDef = new WorkflowWidgetDefinition();
        wwDef.setIdentifier(generateIdentifier(type, label));
        wwDef.setType(type.name());
        List<WorkflowWidgetDefinitionParameter> wwPars = new LinkedList<WorkflowWidgetDefinitionParameter>();
        WorkflowWidgetDefinitionParameter wwPar = new WorkflowWidgetDefinitionParameter();
        wwPar.setKey(WorkflowWidget.WW_BUTTON_LABEL);
        wwPar.setValue(label);
        wwDef.getParameters().add(wwPar);
        return wwDef;
    }

    private String generateIdentifier(WWType type, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append(WorkflowWidget.WW_BUTTON_LABEL).append("=\"").append(label).append("\"\n");
        return type.name() + sb.toString().hashCode();
    }

    private boolean isNotesWidget(WorkflowWidgetDefinition w) {
        return "openNotes".equalsIgnoreCase(w.getType());
    }

    private boolean isAttachmentWidget(WorkflowWidgetDefinition w) {
        return "openAttchment".equalsIgnoreCase(w.getType());
    }

    private String generateProcessForm(ActivitySchema activity, CardConfiguration cardConfig, PortletLayout portletLayout, boolean compiled) {
        StringBuilder result = new StringBuilder();
        FormSerializer form = new FormSerializer(contextPath);
        result.append(form.processFormLayoutHeader(compiled));
        if (compiled) {
            result.append(createCompiledWorkflowForm(activity, cardConfig, portletLayout));
        } else {
            result.append(createEmptyWorkflowForm(activity, cardConfig, portletLayout));
        }

        ButtonLayout buttonLayout = new ButtonLayout();
        result.append(buttonLayout.generateButtons(Types.Buttons.EMPTY_CARD, cardConfig, true));
        result.append(form.formLayoutFooter());
        return result.toString();
    }
}