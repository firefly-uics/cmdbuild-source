<%@page import="org.cmdbuild.portlet.Log"%>
<%@page import="org.cmdbuild.portlet.configuration.PortletConfiguration"%>
<%@page import="com.liferay.portal.service.UserLocalServiceUtil"%>
<%@page import="org.cmdbuild.portlet.metadata.User"%>
<%

    String contextPath = renderRequest.getContextPath();
    String user = PortletConfiguration.getInstance().getTrustedService();
    String password = PortletConfiguration.getInstance().getServicePassword();
    String url = PortletConfiguration.getInstance().getCmdbuildUrl();
    String group = PortletConfiguration.getInstance().getServiceGroup();
    String supportEmail = PortletConfiguration.getInstance().getSupportEmail();
    String cmdbusertable = PortletConfiguration.getInstance().getCMDBuildUserClass();
    boolean displayEmailColumn = PortletConfiguration.getInstance().displayEmailColumn();
    boolean displayDetailColumn = PortletConfiguration.getInstance().displayDetailColumn();
    boolean displayOnlyBaseDSP = PortletConfiguration.getInstance().displayOnlyBaseDSP();
    boolean advanceProcess = PortletConfiguration.getInstance().displayAdvanceProcess();
    boolean displayHistory = PortletConfiguration.getInstance().displayHistory();
    boolean displayWorkflowNotes = PortletConfiguration.getInstance().forceDisplayWorkflowNotes();
    boolean displayWorkflowAttachments = PortletConfiguration.getInstance().forceDisplayWorkflowAttachments();
    boolean displayWorkflowHelp = PortletConfiguration.getInstance().displayWorkflowHelp();
    boolean displayWorkflowWidgets = PortletConfiguration.getInstance().displayWorkflowWidgets();
    boolean displayAttachmentList = PortletConfiguration.getInstance().displayAttachmentList();
    boolean displayOpenedProcesses = PortletConfiguration.getInstance().displayOpenedProcesses();
    boolean displayStartProcess = PortletConfiguration.getInstance().displayStartProcess();

    String connectedUserId = renderRequest.getRemoteUser();
    String userEmail = UserLocalServiceUtil.getUserById(Integer.parseInt(connectedUserId)).getEmailAddress();
    String connectedUser = userEmail.split("@")[0];
    String userFirstName = UserLocalServiceUtil.getUserById(Integer.parseInt(connectedUserId)).getFirstName();
    String userLastname = UserLocalServiceUtil.getUserById(Integer.parseInt(connectedUserId)).getLastName();

    Log.PORTLET.debug("Connected user email: " + userEmail);
    Log.PORTLET.debug("Connected user (appendix of email): " + connectedUser);
    Log.PORTLET.debug("Connected user first name: " + userFirstName);
    Log.PORTLET.debug("Connected user last name: " + userLastname);
    if (!"".equals(group)){
        user = user + "#" + connectedUser + "@" + group;
    } else {
        user = user + "#" + connectedUser;
    }
    Log.PORTLET.debug("Trying to connect to to CMDBuild with user " + user);
    request.getSession().setAttribute("connectedUser", connectedUser);
    request.getSession().setAttribute("useremail", userEmail);
    request.getSession().setAttribute("displayEmailColumn", displayEmailColumn);
    request.getSession().setAttribute("advanceProcess", advanceProcess);
    request.getSession().setAttribute("displayDetailColumn", displayDetailColumn);
    request.getSession().setAttribute("displayOnlyBaseDSP", displayOnlyBaseDSP);
    request.getSession().setAttribute("displayHistory", displayHistory);
    request.getSession().setAttribute("displayWorkflowNotes", displayWorkflowNotes);
    request.getSession().setAttribute("displayWorkflowAttachments", displayWorkflowAttachments);
    request.getSession().setAttribute("displayWorkflowHelp", displayWorkflowHelp);
    request.getSession().setAttribute("displayWorkflowWidgets", displayWorkflowWidgets);
    request.getSession().setAttribute("displayAttachmentList", displayAttachmentList);
    request.getSession().setAttribute("contextPath", contextPath);
%>
