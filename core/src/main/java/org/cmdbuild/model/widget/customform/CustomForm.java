package org.cmdbuild.model.widget.customform;

import java.util.Map;

import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.codehaus.jackson.annotate.JsonTypeInfo;

public class CustomForm extends Widget {

	public static class Capabilities {

		private boolean addDisabled;
		private boolean deleteDisabled;
		private boolean importCsvDisabled;

		public boolean isAddDisabled() {
			return addDisabled;
		}

		public void setAddDisabled(final boolean addDisabled) {
			this.addDisabled = addDisabled;
		}

		public boolean isDeleteDisabled() {
			return deleteDisabled;
		}

		public void setDeleteDisabled(final boolean deleteDisabled) {
			this.deleteDisabled = deleteDisabled;
		}

		public boolean isImportCsvDisabled() {
			return importCsvDisabled;
		}

		public void setImportCsvDisabled(final boolean importCsvDisabled) {
			this.importCsvDisabled = importCsvDisabled;
		}

	}

	public static class Serialization {

		@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "__type__")
		public static interface Configuration {

		}

		private String type;
		private Configuration configuration;

		public String getType() {
			return type;
		}

		public void setType(final String type) {
			this.type = type;
		}

		public Configuration getConfiguration() {
			return configuration;
		}

		public void setConfiguration(final Configuration configuration) {
			this.configuration = configuration;
		}

	}

	public static class TextConfiguration implements Serialization.Configuration {

		private String keyValueSeparator;
		private String attributesSeparator;
		private String rowsSeparator;

		public String getKeyValueSeparator() {
			return keyValueSeparator;
		}

		public void setKeyValueSeparator(final String keyValueSeparator) {
			this.keyValueSeparator = keyValueSeparator;
		}

		public String getAttributesSeparator() {
			return attributesSeparator;
		}

		public void setAttributesSeparator(final String attributesSeparator) {
			this.attributesSeparator = attributesSeparator;
		}

		public String getRowsSeparator() {
			return rowsSeparator;
		}

		public void setRowsSeparator(final String rowsSeparator) {
			this.rowsSeparator = rowsSeparator;
		}

	}

	private boolean required;
	private boolean readOnly;
	private String form;
	private String layout;
	private Capabilities capabilities;
	private Serialization serialization;
	private Map<String, Object> variables;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		// TODO Auto-generated method stub
		super.save(activityInstance, input, output);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getForm() {
		return form;
	}

	public void setForm(final String form) {
		this.form = form;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(final String layout) {
		this.layout = layout;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(final Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public Serialization getSerialization() {
		return serialization;
	}

	public void setSerialization(final Serialization serialization) {
		this.serialization = serialization;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	public void setVariables(final Map<String, Object> variables) {
		this.variables = variables;
	}

}