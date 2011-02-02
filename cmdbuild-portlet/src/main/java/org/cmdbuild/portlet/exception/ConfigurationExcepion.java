package org.cmdbuild.portlet.exception;

public class ConfigurationExcepion extends CMDBuildPortletException {

    private ConfigurationExcepionType type;

    public enum ConfigurationExcepionType {

        PARAMETER_NOT_FOUND;

        public ConfigurationExcepion createException(String... parameters) {
            return new ConfigurationExcepion(this, parameters);
        }
    }

    private ConfigurationExcepion(ConfigurationExcepionType type, String... parameters) {
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
