package org.cmdbuild.portlet.layout;

import java.util.List;

import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Metadata;

public class ComponentLayout {

	private List<Metadata> metadata;
	private String classname;
	private AttributeSchema schema;
	private String value;
	private String id;
	private boolean visible;
	private String userLogin;
	private SOAPClient client;
	private String contextPath;

	public ComponentLayout() {
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public List<Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(final List<Metadata> metadata) {
		this.metadata = metadata;
	}

	public AttributeSchema getSchema() {
		return schema;
	}

	public void setSchema(final AttributeSchema schema) {
		this.schema = schema;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getType() {
		return schema.getType();
	}

	public boolean isLimited() {
		return schema.getLength() > 0 && getType().equals("STRING");
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public SOAPClient getClient() {
		return client;
	}

	public void setClient(final SOAPClient client) {
		this.client = client;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(final String login) {
		this.userLogin = login;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(final String classname) {
		this.classname = classname;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(final String contextPath) {
		this.contextPath = contextPath;
	}

}
