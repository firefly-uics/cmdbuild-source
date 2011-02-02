package org.cmdbuild.portlet.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.cmdbuild.portlet.Log;

public class PortletConfiguration {

    private Properties props;
    private static PortletConfiguration instance;
    private static final int MEGABYTE_MULTIPLIER = 1024 * 1024; //1*MEGABYTE_MULTIPLIER=1Mb

    private PortletConfiguration() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("portlet.properties");
        props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException ex) {
            Log.PORTLET.warn("Error getting portlet properties from configuration file", ex);
            ex.printStackTrace();
        }
    }

    public static PortletConfiguration getInstance() {
        if (instance == null) {
            instance = new PortletConfiguration();
        }
        return instance;
    }

    public String getTrustedService() {
        String defaultuser = props.getProperty("defaultuser");
        return defaultuser;
    }

    public String getServicePassword() {
        String defaultpassword = props.getProperty("defaultpassword");
        return defaultpassword;
    }

    public String getServiceGroup() {
        String defaultgroup = props.getProperty("defaultgroup");
        return defaultgroup;
    }

    public String getCMDBuildUserClass() {
        String userclass = props.getProperty("user.class");
        Log.PORTLET.debug("User classname: " + userclass);
        return userclass;
    }

    public String getCMDBuildUserEmail() {
        String keyvalue = props.getProperty("user.email");
        Log.PORTLET.debug("User email value: " + keyvalue);
        return keyvalue;
    }

    public String getDMSLookup() {
        String lookup = props.getProperty("dms.lookup");
        Log.PORTLET.debug("DMS Lookup " + lookup);
        return lookup;
    }

    public String getCmdbuildUrl() {
        String url = props.getProperty("cmdbuildurl");
        char lastChar = url.charAt(url.length() - 1);
        if (lastChar == '/') {
            url = url + "services/soap/Private/";
        } else {
            url = url + "/services/soap/Private/";
        }
        Log.PORTLET.debug("CMDBuild url " + url);
        return url;
    }

    public String getSMTPAddress() {
        String smtpserver = props.getProperty("mail.smtpserver");
        return smtpserver;
    }

    public String getSMTPPort() {
        String port = props.getProperty("mail.port");
        return port;
    }

    public String getSMTPUser() {
        String smtpuser = props.getProperty("mail.user");
        return smtpuser;
    }

    public String getSMTPPassword() {
        String smtppass = props.getProperty("mail.password");
        return smtppass;
    }

    public boolean useSSL() {
        String ssl = props.getProperty("mail.ssl");
        Log.PORTLET.debug("Use SSL? " + ssl);
        boolean usessl = false;
        if (ssl != null && !ssl.equals("")) {
            usessl = Boolean.valueOf(ssl);
        }
        return usessl;
    }

    public String getSupportEmail() {
        String supportemail = props.getProperty("supportemail");
        return supportemail;
    }

    public String getWorkflowEmail() {
        String supportemail = props.getProperty("workflow.email");
        return supportemail;
    }

    public boolean displayOpenedProcesses() {
        boolean showprocesses = Boolean.valueOf(props.getProperty("workflow.display.opened"));
        return showprocesses;
    }

    public String getLayoutTextareaRows() {
        String rows = props.getProperty("layout.textarea");
        return rows;
    }

    public int getMaxUploadSize() {
        int size = Integer.parseInt(props.getProperty("dms.maxuploadsize")) * MEGABYTE_MULTIPLIER;
        return size;
    }

    public int getSoapTimeout() {
        int timeout = Integer.parseInt(props.getProperty("soap.timeout")) * 1000;
        return timeout;
    }

    public boolean displayEmailColumn() {
        boolean display = Boolean.parseBoolean(props.getProperty("mail.display.column"));
        return display;
    }

    public boolean forceDisplayWorkflowNotes() {
        boolean display = Boolean.parseBoolean(props.getProperty("workflow.force.note"));
        return display;
    }

    public boolean forceDisplayWorkflowAttachments() {
        boolean display = Boolean.parseBoolean(props.getProperty("workflow.force.attachments"));
        return display;
    }

    public boolean displayWorkflowHelp() {
        boolean display = Boolean.parseBoolean(props.getProperty("workflow.display.help"));
        return display;
    }

    public boolean displayWorkflowWidgets() {
        boolean display = Boolean.parseBoolean(props.getProperty("workflow.display.widgets"));
        return display;
    }

    public int maxReferenceToDisplay() {
        int display = Integer.valueOf(props.getProperty("layout.referencecombo"));
        return display;
    }

    public boolean displayStartProcess() {
        boolean start = Boolean.parseBoolean(props.getProperty("workflow.display.start"));
        return start;
    }

    public boolean displayAdvanceProcess() {
        boolean advance = Boolean.parseBoolean(props.getProperty("workflow.display.advance"));
        return advance;
    }

    public boolean displayDetailColumn() {
        boolean detail = Boolean.parseBoolean(props.getProperty("workflow.display.detail"));
        return detail;
    }

    public boolean displayOnlyBaseDSP() {
        boolean baseDSP = Boolean.parseBoolean(props.getProperty("layout.onlybasedsp"));
        return baseDSP;
    }

    public boolean displayHistory() {
        boolean history = Boolean.parseBoolean(props.getProperty("layout.history"));
        return history;
    }

    public boolean displayAttachmentList() {
        boolean attachments = Boolean.parseBoolean(props.getProperty("workflow.display.attachments"));
        return attachments;
    }

    public String[] getPlugins() {
        String plugins = props.getProperty("plugins");
        if (plugins != null && plugins.trim().length() > 0) {
            return plugins.split(",");
        } else {
            return new String[0];
        }
    }

    public String getGridOrderColumn() {
        String column = props.getProperty("grid.order.column");
        return column;
    }

    public String getGridOrderDirection() {
        String direction = props.getProperty("grid.order.direction");
        if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
            direction = "ASC";
        }
        return direction.toUpperCase();
    }
}
