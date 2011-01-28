package org.cmdbuild.portlet.layout;

import java.util.List;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.metadata.CMDBuildTagDictionary;
import org.cmdbuild.portlet.metadata.User;
import org.cmdbuild.portlet.operation.ReferenceOperation;
import org.cmdbuild.portlet.utils.FieldUtils;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Metadata;
import org.cmdbuild.services.soap.Reference;

public class ReferenceComponentSerializer implements HTMLSerializer {

    private final ComponentLayout layout;

    public ReferenceComponentSerializer(ComponentLayout layout) {
        this.layout = layout;
    }

    public String serializeHtml() {
        FieldUtils utils = new FieldUtils();
        AttributeSchema schema = layout.getSchema();
        String description = utils.setMandatoryField(schema);
        StringBuilder result = new StringBuilder();
        result.append("<div class=\"CMDBuildRow\">\n<label class=\"CMDBuildCol1\">").append(utils.checkString(description)).append("</label>\n");
        ReferenceOperation operation = new ReferenceOperation(layout.getClient());
        List<Reference> referenceArray = operation.getReferenceList(layout, PortletConfiguration.getInstance().maxReferenceToDisplay(), 0);
        String id = "cmdbcombo-" + schema.getReferencedClassName();
        String referencecombo = generateReferenceComboWithMetadata(referenceArray, id);
        result.append(referencecombo);
        result.append("</div>\n");
        return result.toString();
    }

    private String generateReferenceComboWithMetadata(List<Reference> referenceArray, String id) {
        StringBuilder result = new StringBuilder();
        AttributeSchema schema = layout.getSchema();
        List<Metadata> metadata = layout.getMetadata();
        String ref = null;
        if (metadata != null) {
            for (Metadata meta : metadata) {
                if (meta.getKey().equals(CMDBuildTagDictionary.USERID)) {
                    ref = generateUserReference(schema, id);
                    break;
                }
            }
        }
        if (ref == null) {
            ref = generateGenericReference(referenceArray, id);
        }
        result.append(ref);
        return result.toString();
    }

    private String generateGenericReference(List<Reference> referenceArray, String id) {
        StringBuilder result = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        AttributeSchema schema = layout.getSchema();
        int limit = PortletConfiguration.getInstance().maxReferenceToDisplay();
        if (referenceArray != null) {
            int rows = referenceArray.get(0).getTotalRows();
            if (rows < limit) {
                result.append(generateComboReference(referenceArray, id));
            } else {
                result.append(generateGridReference());
            }
        } else {
            result.append(String.format("<span class=\"CMDBuildCol2\"><select name='%s' class='CMDBuildCmdbreference CMDBuildCol2 %s'>", utils.checkString(schema.getName()), id));
            result.append("<option value=''>  </option>\n");
            result.append("</select>\n");
            result.append("</span>\n");
        }
        return result.toString();
    }

    private String generateComboReference(List<Reference> references, String id) {
        StringBuilder result = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        User user = utils.getUserInformation(layout.getClient(), layout.getUseremail());
        String required = "";
        AttributeSchema schema = layout.getSchema();
        if (utils.isRequired(schema)) {
            required = " required ";
        }
        result.append(String.format("<span class=\"CMDBuildCol2\"><select name='%s' class='CMDBuildCmdbreference CMDBuildCol2 %s %s %s'", utils.checkString(schema.getName()), id, schema.getName(), required));
        if (PortletLayout.READONLY.equals(schema.getFieldmode()) || !layout.isVisible()) {
            result.append(" disabled=\"disabled\" ");
        }
        result.append(">\n");
        if (layout.getValue() != null && !("".equals(layout.getValue()))) {
            result.append(String.format("<option value='%s' selected='selected'>%s</option>", layout.getId(), utils.checkString(layout.getValue())));
        }
        result.append("<option value=''>  </option>");
        for (Reference reference  : references) {
            result.append("<option value=\"").append(reference.getId()).append("\"");
            if (user != null && reference.getId() == user.getId()) {
                result.append("selected=\"selected\"");
            }
            result.append(">").append(utils.checkString(reference.getDescription())).append("</option>\n");
        }
        result.append("</select>\n");
        result.append(generateClearReferenceButton(id, schema.getFieldmode()));
        result.append("</span>\n");
        return result.toString();
    }

    private String generateUserReference(AttributeSchema schema, String id) {
        StringBuilder result = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        User user = utils.getUserInformation(layout.getClient(), layout.getUseremail());
        String required = "";
        if (utils.isRequired(schema)) {
            required = " required ";
        }
        result.append("<span class=\"CMDBuildCol2\">");
        result.append(String.format("<select name='%s' class='CMDBuildCmdbreference CMDBuildCol2 %s %s'>\n ", utils.checkString(schema.getName()), required, id));
        if (user != null) {
            result.append(String.format("<option value='%d' selected='selected'>%s</option>\n", user.getId(), user.getName()));
        } else {
            result.append("<option value='' selected=\"selected\">  </option>\n");
        }
        result.append("</select>\n");
        result.append(generateClearReferenceButton(id, schema.getFieldmode()));
        result.append("</span>\n");
        return result.toString();
    }

    private String generateClearReferenceButton(String id, String fieldmode) {
        StringBuilder result = new StringBuilder();
        String resetFieldScript = String.format("onclick=\"CMDBuildResetField('%s')\"", id);
        result.append("<button type=\"button\" ").append(resetFieldScript).append(" class=\"CMDBuildResetCombo\"");
        if ("read".equalsIgnoreCase(fieldmode)) {
            result.append(" disabled=\"disabled\" ");
        }
        result.append(">x</button>\n");
        return result.toString();
    }

    private String generateGridReference() {
        StringBuilder result = new StringBuilder();
        FieldUtils utils = new FieldUtils();
        AttributeSchema schema = layout.getSchema();
        String cname = utils.checkString(schema.getReferencedClassName());
        String id = "CMDBuildReference_" + cname;
        String required = "";
        if (utils.isRequired(schema)) {
            required = " required ";
        }
        result.append("<span class=\"CMDBuildCol2\">\n");
        result.append(String.format("<select id='%s' onclick=\"CMDBuildShowReferenceGrid('%s')\" name='%s' class='cmdbreference CMDBuildCol2 %s %s %s' ", id, cname, utils.checkString(schema.getName()), required, schema.getName(), id));
        if (PortletLayout.READONLY.equals(schema.getFieldmode()) || !layout.isVisible()) {
           result.append(" disabled=\"disabled\" ");
        }
        result.append(">\n");
        result.append("<option value=''>  </option>\n");
        if (layout.getValue() != null && !("".equals(layout.getValue()))) {
            result.append("<option value=\'").append(layout.getId()).append("\' selected=\"selected\">").append(utils.checkString(layout.getValue())).append("</option>\n");
        }
        result.append("</select>\n");
        result.append(generateClearReferenceButton(id, schema.getFieldmode()));
        result.append("</span>\n");
        return result.toString();
    }

}
