package org.cmdbuild.portlet.layout;

import java.util.List;
import org.cmdbuild.portlet.layout.widget.WorkflowWidget;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.Types;
import org.cmdbuild.portlet.configuration.ButtonBarConfiguration;

public class ButtonLayout {

    private static final String PROCESS_TYPE = "process";

    public String generateButtons(Types.Buttons buttonType, CardConfiguration config, boolean editable) {
        String buttons = "";
        switch (buttonType) {
            case EMPTY_CARD:
                buttons = generateCardButtonCode(config, 0, editable);
                break;
            case COMPILED_CARD:
                buttons = generateCardButtonCode(config, config.getId(), editable);
                break;
            case ATTACHMENT_FORM:
                buttons = generateGenericButtonCode("Allega");
                break;
            case REPORT_FORM:
                buttons = generateReportButtonCode();
                break;
            case LINK_CARDS_FORM:
                buttons = generateLinkCardButtonCode();
                break;
        }
        return buttons;
    }

    public String generateProcessButtonBar(ButtonBarConfiguration config, List<WorkflowWidget> widget, boolean advanceProcess) {
        StringBuilder layout = new StringBuilder();
        String advance = "";
        if (advanceProcess) {
            advance = "Advance";
        }
        layout.append("<div class=\"CMDBuildProcessbuttonbarContainer\">");
        layout.append("<ul id=\"CMDBuild").append(advance).append("Processbuttonbar\" >");
        layout.append("<li id=\"CMDBuild").append(advance).append("ProcessForm\" ><div class=\"CMDBuildButtonBarElement\">Scheda</div></li>");
        if (config.isDisplayWorkflowWidget()) {
            layout.append(generateWorkflowWidgetComponents(widget));
        }
        if (config.isDisplayHelp()) {
            layout.append("<li id=\"CMDBuild").append(advance).append("Help\" ><div class=\"CMDBuildButtonBarElement\">Help</div></li>");
        }
        layout.append("</ul>");
        layout.append("</div>");
        return layout.toString();
    }

    private String generateWorkflowWidgetComponents(List<WorkflowWidget> widgets) {
        StringBuilder layout = new StringBuilder();
        if (widgets != null) {
            for (WorkflowWidget widget : widgets) {
                String label = widget.getLabel();
                String requiredClass = "";
                if (widget.isRequired()) {
                    label = label + " *";
                    requiredClass = " CMDBuildWWRequired ";
                }
                layout.append(String.format("<li id=\"%s\" class=\"%s %s\" ><div class=\"CMDBuildButtonBarElement\">%s</div></li>", widget.getIdentifier(), widget.getType(), requiredClass, label));
            }
        }
        return layout.toString();
    }

    private String generateCardButtonCode(CardConfiguration config, int id, boolean editable) {
        StringBuilder layout = new StringBuilder();
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\" ";
        }
        String javascript = "onclick=\"CMDBuildCleanForm('" + config.getType() + "')\"";
        if (config.getType().equals(PROCESS_TYPE)) {
            layout.append("<p class=\"CMDBuildProcessbuttons\">\n");
        } else {
            layout.append("<p class=\"CMDBuildCardbuttons\">\n");
        }
        layout.append("<input type=\"submit\" value=\"Invia\"").append(disabled).append("/>\n");
        layout.append("<input type=\"reset\"  value=\"Annulla\" class=\"CMDBuildResetbutton\"").append(disabled).append(javascript).append(" />\n");
        layout.append("<input type=\"hidden\" name=\"classname\" value=\"").append(config.getClassname()).append("\" />\n");
        if (id > 0) {
            layout.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\" />\n");
        }
        layout.append("</p>\n");
        return layout.toString();
    }

    private String generateGenericButtonCode(String buttonLabel) {
        StringBuilder layout = new StringBuilder();
        layout.append("<p class=\"CMDBuildCardbuttons\">\n");
        layout.append(String.format("<input type=\"submit\" value=\"%s\" />\n", buttonLabel));
        layout.append("</p>\n");
        return layout.toString();
    }

    private String generateReportButtonCode() {
        StringBuilder layout = new StringBuilder();
        layout.append("<p class=\"CMDBuildCardbuttons\">\n");
        layout.append("<input type=\"submit\" value=\"Invia\" class=\"CMDBuildReportSubmit\" />\n");
        layout.append("</p>\n");
        return layout.toString();
    }

    private String generateLinkCardButtonCode() {
        StringBuilder layout = new StringBuilder();
        layout.append("<p class=\"CMDBuildLinkCardbuttons\">\n");
        layout.append("<input type=\"submit\" value=\"Conferma\" class=\"CMDBuildLinkCardSubmit\" />\n");
        layout.append("</p>\n");
        return layout.toString();
    }
}
