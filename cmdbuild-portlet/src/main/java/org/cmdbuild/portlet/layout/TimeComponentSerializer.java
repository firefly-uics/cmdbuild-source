package org.cmdbuild.portlet.layout;

import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.AttributeSchema;

public class TimeComponentSerializer implements HTMLSerializer {

    private final ComponentLayout layout;

    public TimeComponentSerializer(ComponentLayout layout) {
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
            required = " required ";
        }
        htmlOutput.append("<div class=\"CMDBuildRow\">")
                .append(String.format("<label class=\"CMDBuildCol1\">%s</label>\n", utils.checkString(description)));
        htmlOutput.append(String.format("<span class=\"CMDBuildCol2\"><input class=\"%s CMDBuildDate_input CMDBuildCol2\"  onfocus=\"CMDBuildShowDateInput()\" ", required))
                    .append(String.format("name=\"%s\" value=\"%s\"", schema.getName(), utils.checkString(layout.getValue())));
        if (PortletLayout.READONLY.equals(fieldmode) || !layout.isVisible()) {
            htmlOutput.append(" disabled=\"disabled\"/></span></div>\n");
        } else {
             htmlOutput.append(" /></span></div>\n");
        }
        return htmlOutput.toString();
    }
}
