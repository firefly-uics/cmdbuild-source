package org.cmdbuild.model.widget.customform;

import java.util.Map;

import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.cmdbuild.workflow.CMActivityInstance;

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

	private boolean required;
	private boolean readOnly;
	private String form;
	private String layout;
	private Capabilities capabilities;
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

	public Map<String, Object> getVariables() {
		return variables;
	}

	public void setVariables(final Map<String, Object> variables) {
		this.variables = variables;
	}

}