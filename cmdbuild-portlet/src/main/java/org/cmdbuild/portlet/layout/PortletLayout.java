package org.cmdbuild.portlet.layout;

import java.util.List;
import java.util.Map;

import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Metadata;

public class PortletLayout {

	public static final String READONLY = "read";
	public static final String HIDDEN = "hidden";
	private final SOAPClient client;
	private final String userLogin;
	private final String contextPath;
	private boolean visible = true;

	enum ElementType {

		STRING() {
			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TextComponentSerializer(layout).serializeHtml();
			}
		},
		INTEGER {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TextComponentSerializer(layout).serializeHtml();
			}
		},
		DECIMAL {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TextComponentSerializer(layout).serializeHtml();
			}
		},
		DOUBLE {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TextComponentSerializer(layout).serializeHtml();
			}
		},
		TEXT {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TextComponentSerializer(layout).serializeHtml();
			}
		},
		BOOLEAN {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new BooleanComponentSerializer(layout).serializeHtml();
			}
		},
		DATE {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TimeComponentSerializer(layout).serializeHtml();
			}
		},
		TIMESTAMP {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new TimeComponentSerializer(layout).serializeHtml();
			}
		},
		LOOKUP {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new LookupComponentSerializer(layout).serializeHtml();
			}
		},
		REFERENCE {

			@Override
			String generateComponent(final ComponentLayout layout) {
				return new ReferenceComponentSerializer(layout).serializeHtml();
			}
		};

		abstract String generateComponent(ComponentLayout layout);
	}

	public PortletLayout(final SOAPClient client, final String userLogin, final String contextPath) {
		this.client = client;
		this.userLogin = userLogin;
		this.contextPath = contextPath;
	}

	public SOAPClient getClient() {
		return client;
	}

	public String getComponent(final String classname, final AttributeSchema schema, String id, String value,
			final boolean visible) {
		this.visible = visible;
		final List<Metadata> metadata = schema.getMetadata();
		if (id == null) {
			id = "";
		}
		if (value == null) {
			value = "";
		}
		final ComponentLayout component = serializeComponent(classname, schema, metadata, id, value, visible);
		return generateComponent(component);
	}

	private String generateComponent(final ComponentLayout layout) {
		final String type = layout.getType();
		final AttributeSchema schema = layout.getSchema();
		if (HIDDEN.equals(schema.getFieldmode())) {
			return "";
		}
		final ElementType layoutType = ElementType.valueOf(type);
		Log.PORTLET.debug(String.format("Getting %s with description %s", layoutType.toString().toLowerCase(), layout
				.getSchema().getDescription()));
		return layoutType.generateComponent(layout);
	}

	public String generateTitle(final String classdescription) {
		final StringBuilder result = new StringBuilder();
		result.append("<h2 class=\"CMDBuildClassTitle\">").append(classdescription).append("</h2>\n");
		return result.toString();
	}

	public String generateElementDetail(final int index, final Map<Integer, Card> cards,
			final List<AttributeSchema> schema) {
		final StringBuilder result = new StringBuilder();
		final Card card = cards.get(index);
		final List<Attribute> attrs = card.getAttributeList();
		result.append("<p>");
		for (final AttributeSchema as : schema) {
			if (attrs != null && attrs.size() > 0) {
				for (final Attribute attribute : attrs) {
					final String name = as.getName();
					final String aname = attribute.getName();
					final String avalue = attribute.getValue();
					if (aname.equals(name) && !(aname.equals("Id") || aname.equals("Notes") || aname.equals("IdClass"))) {
						result.append("<span style=\"font-weight:bold\">").append(as.getDescription()).append(
								": </span>").append(avalue).append("<br />");
					}
				}
			}
		}
		result.append("</p>");
		return result.toString();
	}

	public ComponentLayout serializeComponent(final String classname, final AttributeSchema schema,
			final List<Metadata> metadata, final String id, final String value, final boolean visible) {
		final ComponentLayout component = new ComponentLayout();
		component.setId(id);
		component.setValue(value);
		component.setClassname(classname);
		component.setMetadata(metadata);
		component.setSchema(schema);
		component.setVisible(visible);
		component.setClient(client);
		component.setUserLogin(userLogin);
		component.setContextPath(contextPath);
		return component;
	}
}
