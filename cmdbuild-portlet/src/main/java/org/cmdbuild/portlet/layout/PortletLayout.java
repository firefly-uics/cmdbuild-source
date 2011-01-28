package org.cmdbuild.portlet.layout;

import java.util.List;
import java.util.Map;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Metadata;

public class PortletLayout {

    public static final String READONLY = "read";
    public static final String HIDDEN = "hidden";
    private SOAPClient client;
    private String useremail;
    private String contextPath;
    private boolean visible = true;

    enum ElementType {

        STRING() {
            @Override
            String generateComponent(ComponentLayout layout) {
                return new TextComponentSerializer(layout).serializeHtml();
            }
        },
        INTEGER {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new TextComponentSerializer(layout).serializeHtml();
            }
        },
        DECIMAL {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new TextComponentSerializer(layout).serializeHtml();
            }
        },
        DOUBLE {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new TextComponentSerializer(layout).serializeHtml();
            }
        },
        TEXT {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new TextComponentSerializer(layout).serializeHtml();
            }
        },
        BOOLEAN {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new BooleanComponentSerializer(layout).serializeHtml();
            }
        },
        DATE {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new TimeComponentSerializer(layout).serializeHtml();
            }
        },
        TIMESTAMP {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new TimeComponentSerializer(layout).serializeHtml();
            }
        },
        LOOKUP {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new LookupComponentSerializer(layout).serializeHtml();
            }
        },
        REFERENCE {

            @Override
            String generateComponent(ComponentLayout layout) {
                return new ReferenceComponentSerializer(layout).serializeHtml();
            }
        };

        abstract String generateComponent(ComponentLayout layout);
    }

    public PortletLayout(SOAPClient client, String useremail, String contextPath) {
        this.client = client;
        this.useremail = useremail;
        this.contextPath = contextPath;
    }

    public SOAPClient getClient() {
        return client;
    }

    public String getComponent(String classname, AttributeSchema schema, String id, String value, boolean visible) {
        this.visible = visible;
        List<Metadata> metadata = schema.getMetadata();
        if (id == null) {
            id = "";
        }
        if (value == null) {
            value = "";
        }
        ComponentLayout component = serializeComponent(classname, schema, metadata, id, value, visible);
        return generateComponent(component);
    }

    private String generateComponent(ComponentLayout layout) {
        String type = layout.getType();
        AttributeSchema schema = layout.getSchema();
        if (HIDDEN.equals(schema.getFieldmode())) {
            return "";
        }
        ElementType layoutType = ElementType.valueOf(type);
        Log.PORTLET.debug(String.format("Getting %s with description %s",
                layoutType.toString().toLowerCase(),
                layout.getSchema().getDescription()));
        return layoutType.generateComponent(layout);
    }

    public String generateTitle(String classdescription) {
        StringBuilder result = new StringBuilder();
        result.append("<h2 class=\"CMDBuildClassTitle\">").append(classdescription).append("</h2>\n");
        return result.toString();
    }

    public String generateElementDetail(int index, Map<Integer, Card> cards, List<AttributeSchema> schema) {
        StringBuilder result = new StringBuilder();
        Card card = cards.get(index);
        List<Attribute> attrs = card.getAttributeList();
        result.append("<p>");
        for (AttributeSchema as : schema) {
            if (attrs != null && attrs.size() > 0) {
                for (Attribute attribute : attrs) {
                    String name = as.getName();
                    String aname = attribute.getName();
                    String avalue = attribute.getValue();
                    if (aname.equals(name) && !(aname.equals("Id") || aname.equals("Notes") || aname.equals("IdClass"))) {
                        result.append("<span style=\"font-weight:bold\">").append(as.getDescription()).append(": </span>").append(avalue).append("<br />");
                    }
                }
            }
        }
        result.append("</p>");
        return result.toString();
    }

    public ComponentLayout serializeComponent(String classname, AttributeSchema schema,  List<Metadata> metadata, String id, String value, boolean visible) {
        ComponentLayout component = new ComponentLayout();
        component.setId(id);
        component.setValue(value);
        component.setClassname(classname);
        component.setMetadata(metadata);
        component.setSchema(schema);
        component.setVisible(visible);
        component.setClient(client);
        component.setUseremail(useremail);
        component.setContextPath(contextPath);
        return component;
    }
}
