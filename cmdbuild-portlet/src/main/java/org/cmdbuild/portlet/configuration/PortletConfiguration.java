package org.cmdbuild.portlet.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.auth.AuthMethod;

public class PortletConfiguration {

	private static final String PORTLET_PROPERTIES = "portlet.properties";
	private static final String PORTLET_EXT_PROPERTIES = "portlet-ext.properties";

	private final Properties properties;
	private static PortletConfiguration instance;
	private static final int MEGABYTE_MULTIPLIER = 1024 * 1024; // 1*MEGABYTE_MULTIPLIER=1Mb

	private PortletConfiguration() {
		final ClassLoader classLoader = this.getClass().getClassLoader();
		final Properties defaultProperties = new Properties();
		try {
			final InputStream inputStream =classLoader.getResourceAsStream(PORTLET_PROPERTIES);  
			defaultProperties.load(inputStream);
		} catch (final IOException ex) {
			Log.PORTLET.error("Error getting portlet properties", ex);
			ex.printStackTrace();
		} finally {
			properties = new Properties(defaultProperties);
		}

		try {
			final InputStream inputStream =classLoader.getResourceAsStream(PORTLET_EXT_PROPERTIES);
			if (inputStream != null) {
				properties.load(inputStream);
			}
			else {
				Log.PORTLET.warn("Missing file " + PORTLET_EXT_PROPERTIES);
			}
		} catch (final IOException ex) {
			Log.PORTLET.warn("Error getting extended portlet properties from configuration file", ex);
			ex.printStackTrace();
		}
	}

	public static PortletConfiguration getInstance() {
		if (instance == null) {
			instance = new PortletConfiguration();
		}
		return instance;
	}

	public String getServiceUser() {
		final String defaultuser = properties.getProperty("defaultuser");
		return defaultuser;
	}

	public String getServicePassword() {
		final String defaultpassword = properties.getProperty("defaultpassword");
		return defaultpassword;
	}

	public String getServiceGroup() {
		final String defaultgroup = properties.getProperty("defaultgroup");
		return defaultgroup;
	}

	public String getCMDBuildUserClass() {
		final String userclass = properties.getProperty("user.class");
		Log.PORTLET.debug("User classname: " + userclass);
		return userclass;
	}

	public String getCMDBuildUserUsername() {
		final String keyvalue = properties.getProperty("user.username");
		Log.PORTLET.debug("User username value: " + keyvalue);
		return keyvalue;
	}

	public String getCMDBuildUserEmail() {
		final String keyvalue = properties.getProperty("user.email");
		Log.PORTLET.debug("User email value: " + keyvalue);
		return keyvalue;
	}

	public String getDMSLookup() {
		final String lookup = properties.getProperty("dms.lookup");
		Log.PORTLET.debug("DMS Lookup " + lookup);
		return lookup;
	}

	public String getCmdbuildUrl() {
		String url = properties.getProperty("cmdbuildurl");
		final char lastChar = url.charAt(url.length() - 1);
		if (lastChar == '/') {
			url = url + "services/soap/Private/";
		} else {
			url = url + "/services/soap/Private/";
		}
		Log.PORTLET.debug("CMDBuild url " + url);
		return url;
	}

	public String getSMTPAddress() {
		final String smtpserver = properties.getProperty("mail.smtpserver");
		return smtpserver;
	}

	public String getSMTPPort() {
		final String port = properties.getProperty("mail.port");
		return port;
	}

	public String getSMTPUser() {
		final String smtpuser = properties.getProperty("mail.user");
		return smtpuser;
	}

	public String getSMTPPassword() {
		final String smtppass = properties.getProperty("mail.password");
		return smtppass;
	}

	public boolean useSSL() {
		final String ssl = properties.getProperty("mail.ssl");
		Log.PORTLET.debug("Use SSL? " + ssl);
		boolean usessl = false;
		if (ssl != null && !ssl.equals(StringUtils.EMPTY)) {
			usessl = Boolean.valueOf(ssl);
		}
		return usessl;
	}

	public String getSupportEmail() {
		final String supportemail = properties.getProperty("supportemail");
		return supportemail;
	}

	public String getWorkflowEmail() {
		final String supportemail = properties.getProperty("workflow.email");
		return supportemail;
	}

	public boolean displayOpenedProcesses() {
		final boolean showprocesses = Boolean.valueOf(properties.getProperty("workflow.display.opened"));
		return showprocesses;
	}

	public String getLayoutTextareaRows() {
		final String rows = properties.getProperty("layout.textarea");
		return rows;
	}

	public int getMaxUploadSize() {
		final int size = Integer.parseInt(properties.getProperty("dms.maxuploadsize")) * MEGABYTE_MULTIPLIER;
		return size;
	}

	public int getSoapTimeout() {
		final int timeout = Integer.parseInt(properties.getProperty("soap.timeout")) * 1000;
		return timeout;
	}

	public boolean displayEmailColumn() {
		final boolean display = Boolean.parseBoolean(properties.getProperty("mail.display.column"));
		return display;
	}

	public boolean forceDisplayWorkflowNotes() {
		final boolean display = Boolean.parseBoolean(properties.getProperty("workflow.force.note"));
		return display;
	}

	public boolean forceDisplayWorkflowAttachments() {
		final boolean display = Boolean.parseBoolean(properties.getProperty("workflow.force.attachments"));
		return display;
	}

	public boolean displayWorkflowHelp() {
		final boolean display = Boolean.parseBoolean(properties.getProperty("workflow.display.help"));
		return display;
	}

	public boolean displayWorkflowWidgets() {
		final boolean display = Boolean.parseBoolean(properties.getProperty("workflow.display.widgets"));
		return display;
	}

	public int maxReferenceToDisplay() {
		final int display = Integer.valueOf(properties.getProperty("layout.referencecombo"));
		return display;
	}

	public boolean displayStartProcess() {
		final boolean start = Boolean.parseBoolean(properties.getProperty("workflow.display.start"));
		return start;
	}

	public boolean displayAdvanceProcess() {
		final boolean advance = Boolean.parseBoolean(properties.getProperty("workflow.display.advance"));
		return advance;
	}

	public boolean displayDetailColumn() {
		final boolean detail = Boolean.parseBoolean(properties.getProperty("workflow.display.detail"));
		return detail;
	}

	public boolean displayOnlyBaseDSP() {
		final boolean baseDSP = Boolean.parseBoolean(properties.getProperty("layout.onlybasedsp"));
		return baseDSP;
	}

	public boolean displayHistory() {
		final boolean history = Boolean.parseBoolean(properties.getProperty("layout.history"));
		return history;
	}

	public boolean displayAttachmentList() {
		final boolean attachments = Boolean.parseBoolean(properties.getProperty("workflow.display.attachments"));
		return attachments;
	}

	public String[] getPlugins() {
		final String plugins = properties.getProperty("plugins");
		if (plugins != null && plugins.trim().length() > 0) {
			return plugins.split(",");
		} else {
			return new String[0];
		}
	}

	public String getGridOrderColumn() {
		final String column = properties.getProperty("grid.order.column");
		return column;
	}

	public String getGridOrderDirection() {
		String direction = properties.getProperty("grid.order.direction");
		if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
			direction = "ASC";
		}
		return direction.toUpperCase();
	}

	public AuthMethod getAuthMethod() {
		final String method = properties.getProperty("auth.method");
		if (method == null) {
			return AuthMethod.UNKNOWN;
		}
		try {
			return AuthMethod.valueOf(method.toUpperCase());
		} catch (final Exception e) {
			return AuthMethod.UNKNOWN;
		}
	}

}
