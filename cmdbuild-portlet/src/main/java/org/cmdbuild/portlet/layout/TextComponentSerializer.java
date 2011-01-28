package org.cmdbuild.portlet.layout;

import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.AttributeSchema;

public class TextComponentSerializer implements HTMLSerializer {

    private final ComponentLayout layout;

    public TextComponentSerializer(ComponentLayout layout) {
        this.layout = layout;
    }

    public String serializeHtml() {
        if (layout.getSchema().getLength() > 100) {
            return addTextArea();
        } else if (layout.getSchema().getLength() > 1){
            return addTextField();
        } else {
            return addDefaultTextArea();
        }
    }

    private String addTextField() {
        StringBuilder htmlOutput = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        AttributeSchema schema = layout.getSchema();
        String fieldmode = schema.getFieldmode();
        String description = utils.setMandatoryField(schema);
        String javascriptFunction = "";
        if (layout.isLimited()) {
            javascriptFunction = String.format(" onkeyup=\"CMDBuildTextCharLimit(this, %s, \'CMDBuildTextFieldInfo_%s\')\" ", schema.getLength(), schema.getName());
        }
        String required = "";
        if (utils.isRequired(schema)) {
            required = " required ";
        }
        htmlOutput.append("<div class=\"CMDBuildRow\"><label class=\"CMDBuildCol1\">").append(utils.checkString(description)).append("</label>\n");
        htmlOutput.append("<span class=\"CMDBuildCol2\"><input class=\"").append(required).append(" CMDBuildCmdbinput CMDBuildCol2\" name=\"")
                .append(utils.checkString(schema.getName())).append("\"");
        if (PortletLayout.READONLY.equals(fieldmode) || !layout.isVisible()) {
            htmlOutput.append(" disabled=\"disabled\" value=\"").append(utils.checkString(layout.getValue())).append("\"/></span></div>\n");
        } else {
            if (schema.getLength() > 0) {
                htmlOutput.append(" maxlength=\"").append(schema.getLength()).append("\" ");
            }
            htmlOutput.append(javascriptFunction)
                    .append(" value=\"").append(utils.checkString(layout.getValue())).append("\"/><br /><div id=\"CMDBuildTextFieldInfo_")
                    .append(schema.getName()).append("\" style=\"display:none, color: #ff0000\" >").append("</div></span></div>\n");
        }
        return htmlOutput.toString();
    }

    private String addTextArea() {
        StringBuilder htmlOutput = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        AttributeSchema schema = layout.getSchema();
        String description = utils.setMandatoryField(schema);
        htmlOutput.append("<div class=\"CMDBuildRow\"><label class=\"CMDBuildCol1\">").append(utils.checkString(description)).append("</label>\n");
        String fieldmode = schema.getFieldmode();
        String rows = PortletConfiguration.getInstance().getLayoutTextareaRows();
        String javascriptFunction = "";
        if (layout.isLimited()) {
            javascriptFunction = String.format("onkeyup=\"CMDBuildTextCharLimit(this, %s, \'CMDBuildTextareaInfo_%s\')\" ", schema.getLength(), schema.getName());
        }
        String required = "";
        if (utils.isRequired(schema)) {
            required = " required ";
        }
        htmlOutput.append("<span class=\"CMDBuildCol2\"><textarea class=\"").append(required)
                    .append("CMDBuildCmdbtext CMDBuildCol2\" name=\"").append(utils.checkString(schema.getName())).append("\"");
        if (PortletLayout.READONLY.equals(fieldmode) || !layout.isVisible()) {
                htmlOutput.append("\" disabled=\"disabled\">").append(utils.checkString(layout.getValue())).append("</textarea></span></div>\n");
        } else {
            htmlOutput.append(" rows=\"").append(rows).append("\" ")
                    .append(javascriptFunction).append(" >").append(utils.checkString(layout.getValue())).append("</textarea><br /><div id=\"CMDBuildTextareaInfo_")
                    .append(schema.getName()).append("\" style=\"display:none, color: #ff0000\" >").append("</div></span></div>\n");
        }
        return htmlOutput.toString();
    }

    private String addDefaultTextArea() {
        FieldUtils utils = new FieldUtils();
        StringBuilder htmlOutput = new StringBuilder();
        String description = utils.setMandatoryField(layout.getSchema());
        htmlOutput.append("<div class=\"CMDBuildRow\">")
                .append(String.format("<label class=\"CMDBuildCol1\">%s</label>", description))
                .append("<span class=\"CMDBuildCol2\">");
        String fieldmode = layout.getSchema().getFieldmode();
        String required = "";
        if (utils.isRequired(layout.getSchema())) {
            required = " required ";
        }
        htmlOutput.append("<textarea class=\"").append(required)
                    .append("CMDBuildCmdbtext CMDBuildCol2\" name=\"").append(utils.checkString(layout.getSchema().getName())).append("\"");
        if (PortletLayout.READONLY.equals(fieldmode) || !layout.isVisible()) {
            htmlOutput.append("\" disabled=\"disabled\">").append(utils.checkString(layout.getValue())).append("</textarea>\n");
        } else {
            htmlOutput.append("/>\n");
        }
        htmlOutput.append("</span>\n");
        htmlOutput.append("</div>\n");
        return htmlOutput.toString();
    }
}
