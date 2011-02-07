package org.cmdbuild.portlet.exception;

public class ConfigurationExcepion extends CMDBuildPortletException {

	private static final long serialVersionUID = 1L;

	private final ConfigurationExcepionType type;

	public enum ConfigurationExcepionType {

		PARAMETER_NOT_FOUND;

		public ConfigurationExcepion createException(final String... parameters) {
			return new ConfigurationExcepion(this, parameters);
		}
	}

	private ConfigurationExcepion(final ConfigurationExcepionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public ConfigurationExcepionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
