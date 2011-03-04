package org.cmdbuild.portlet.layout;

import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.AttributeSchema;

public class TimeComponentSerializer implements HTMLSerializer {

	private final ComponentLayout layout;

	public TimeComponentSerializer(final ComponentLayout layout) {
		this.layout = layout;

	}

	public String serializeHtml() {
		final StringBuilder htmlOutput = new StringBuilder();
		final AttributeSchema schema = layout.getSchema();
		final String fieldmode = schema.getFieldmode();
		final FieldUtils utils = new FieldUtils();
		final String description = utils.setMandatoryField(schema);
		String required = "";
		if (utils.isRequired(schema)) {
			required = " required ";
		}
		htmlOutput.append("<div class=\"CMDBuildRow\">").append(
				String.format("<label class=\"CMDBuildCol1\">%s</label>\n", utils.checkString(description)));
		htmlOutput
				.append(
						String
								.format(
										"<span class=\"CMDBuildCol2\"><input class=\"%s CMDBuildDate_input CMDBuildCol2\"  onfocus=\"CMDBuildShowDateInput()\" ",
										required)).append(
						String.format("name=\"%s\" value=\"%s\"", schema.getName(), utils
								.checkString(layout.getValue())));
		if (PortletLayout.READONLY.equals(fieldmode) || !layout.isVisible()) {
			htmlOutput.append(" disabled=\"disabled\"/></span></div>\n");
		} else {
			htmlOutput.append(" /></span></div>\n");
		}
		return htmlOutput.toString();
	}
}
