<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.cmdbuild.auth.UserStore" %>
<%@ page import="org.cmdbuild.auth.user.OperationUser" %>
<%@ page import="org.cmdbuild.services.SessionVars" %>
<%@ page import="org.cmdbuild.servlets.json.Login" %>
<%@ page import="org.cmdbuild.spring.SpringIntegrationUtils" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%
	final String lang = SpringIntegrationUtils.applicationContext().getBean(SessionVars.class).getLanguage();
	final OperationUser operationUser = SpringIntegrationUtils.applicationContext().getBean(UserStore.class).getUser();
	final String extVersion = "4.2.0";
%>

<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all.css" />
		<link rel="icon" type="image/x-icon" href="images/favicon.ico" />

		<!-- 0. ExtJS -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/ext-all.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/Notification.js"></script>

		<!-- 1. Main script -->
		<script type="text/javascript" src="javascripts/cmdbuild/core/Utils.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/LoaderConfig.js"></script>
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/application.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/interfaces/Ajax.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Message.js"></script>

		<!-- 2. Translations -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/locale/ext-lang-<%= lang %>.js"></script>
		<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>

		<!-- 3. Runtime configuration -->
		<script type="text/javascript">
			Ext.ns('CMDBuild.configuration.runtime'); // Runtime configurations

			CMDBuild.configuration.runtime = Ext.create('CMDBuild.model.configuration.Runtime');

			<% if (!operationUser.isValid() && !operationUser.getAuthenticatedUser().isAnonymous()) { %>
				CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.USERNAME, '<%= StringEscapeUtils.escapeEcmaScript(operationUser.getAuthenticatedUser().getUsername()) %>');
				CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.GROUPS, <%= Login.serializeGroupForLogin(operationUser.getAuthenticatedUser().getGroupNames()) %>);

				/**
				 * Compatibility mode
				 *
				 * @deprecated (CMDBuild.configuration.runtime)
				 */
				Ext.ns('CMDBuild.Runtime');
				CMDBuild.Runtime.Username = '<%= StringEscapeUtils.escapeEcmaScript(operationUser.getAuthenticatedUser().getUsername()) %>';
				CMDBuild.Runtime.Groups = <%= Login.serializeGroupForLogin(operationUser.getAuthenticatedUser().getGroupNames()) %>;
			<% } %>
		</script>

		<!-- 4. Modules -->
		<script type="text/javascript" src="javascripts/cmdbuild/Login.js"></script>

		<title>CMDBuild</title>
	</head>
	<body>
		<div id="header">
			<img alt="CMDBuild logo" src="images/logo.jpg" />
			<div id="header_po">Open Source Configuration and Management Database</div>
		</div>

		<div id="release_box">
			<span class="x-panel-header-text-default">CMDBuild <tr:translation key="release" /></span>
		</div>
	</body>
</html>