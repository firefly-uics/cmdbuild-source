package org.cmdbuild.portlet.layout;

import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.AttributeSchema;

public class BooleanComponentSerializer implements HTMLSerializer{
    
    private final ComponentLayout layout;
    
    public BooleanComponentSerializer (ComponentLayout layout){
        this.layout = layout;
    }

    public String serializeHtml() {
        StringBuilder htmlOutput = new StringBuilder();
        AttributeSchema schema = layout.getSchema();
        String fieldmode = schema.getFieldmode();
        FieldUtils utils = new FieldUtils();
        String description = utils.setMandatoryField(schema);
        String required = "";
        if (utils.isRequired(schema)) {
            required = " class=\"validate[required]\" ";
        }
        htmlOutput.append("<div class=\"CMDBuildRow\"><label class=\"CMDBuildCol1\">").append(utils.checkString(description)).append("</label>\n");
        if (layout.getValue().equalsIgnoreCase("true")) {
            if ("read".equals(fieldmode) || !layout.isVisible()) {
                htmlOutput.append("<span class=\"CMDBuildCol2\"><input id=\"").append(utils.checkString(schema.getName())).append("\"type=\"checkbox\" name=\"").append(utils.checkString(schema.getName())).append("\" value=\"true\" ").append(required).append(" checked=\"checked\" onclick=\"CMDBuildChangeCheckboxValue(this)\" disabled=\"disabled\"/></span></div>\n");
            } else {
                htmlOutput.append("<span class=\"CMDBuildCol2\"><input id=\"").append(utils.checkString(schema.getName())).append("\"type=\"checkbox\" name=\"").append(utils.checkString(schema.getName())).append("\" value=\"true\" ").append(required).append(" checked=\"checked\" onclick=\"CMDBuildChangeCheckboxValue(this)\"/></span></div>\n");
            }
        } else {
            if ("read".equals(fieldmode) || !layout.isVisible()) {
                htmlOutput.append("<span class=\"CMDBuildCol2\"><input id=\"").append(utils.checkString(schema.getName())).append("\"type=\"checkbox\" name=\"").append(utils.checkString(schema.getName())).append("\" value=\"false\" ").append(required).append(" onclick=\"CMDBuildChangeCheckboxValue(this)\" disabled=\"disabled\"/></span></div>\n");
            } else {
                htmlOutput.append("<span class=\"CMDBuildCol2\"><input id=\"").append(utils.checkString(schema.getName())).append("\"type=\"checkbox\" name=\"").append(utils.checkString(schema.getName())).append("\" value=\"false\" ").append(required).append(" onclick=\"CMDBuildChangeCheckboxValue(this)\"/></span></div>\n");
            }
        }
        return htmlOutput.toString();
    }
}
