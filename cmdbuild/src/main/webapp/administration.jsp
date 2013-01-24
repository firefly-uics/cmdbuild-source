<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.cmdbuild.services.SessionVars"%>
<%@ page import="org.cmdbuild.services.auth.UserContext"%>
<%@ page import="org.cmdbuild.services.auth.Group"%>
<%@ page import="org.cmdbuild.services.auth.User"%>

<%
	SessionVars sessionVars = new SessionVars();
	String lang = sessionVars.getLanguage();
	UserContext userCtx = sessionVars.getCurrentUserContext();
	User user = userCtx.getUser();
	Group defaultGroup = userCtx.getDefaultGroup();
	String extVersion = "4.1.0";
	if (!userCtx.privileges().isAdmin())
		response.sendRedirect("management.jsp");
%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="icon" href="images/favicon.ico" />

		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all-gray.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>-ux/css/portal.css" />

		<%@ include file="libsJsFiles.jsp"%>
		<script type="text/javascript">
			Ext.ns('CMDBuild.Runtime'); // runtime configurations
			CMDBuild.Runtime.UserId = <%= user.getId() %>;
			CMDBuild.Runtime.Username = "<%= user.getName() %>";

			CMDBuild.Runtime.DefaultGroupId = <%= defaultGroup.getId() %>;
			CMDBuild.Runtime.DefaultGroupName = '<%= defaultGroup.getName() %>';
<%	if (userCtx.getGroups().size() == 1) { %>
			CMDBuild.Runtime.LoginGroupId = <%= defaultGroup.getId() %>;
<%	} %>
			CMDBuild.Runtime.AllowsPasswordLogin = <%= userCtx.allowsPasswordLogin() %>;
		</script>
		<script type="text/javascript" src="javascripts/cmdbuild/application.js"></script>
		<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>

		<%@ include file="coreJsFiles.jsp"%>
		<%@ include file="administrationJsFiles.jsp"%>
<!--
		<script type="text/javascript" src="javascripts/cmdbuild/cmdbuild-core.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/cmdbuild-administration.js"></script>
-->
	
	<script type="text/javascript">
	Ext.onReady(function() {
		CMDBuild.app.Administration.init();
	});
	</script>
		<title>CMDBuild</title>
	</head>
	<body id="cmbodyAdministration">
		<div id="header" class="cm_no_display">
			<a href="http://www.cmdbuild.org" target="_blank"><img alt="CMDBuild logo" src="images/logo.jpg" /></a>
			<div id="instance_name"></div>
			<div id="header_po">Open Source Configuration and Management Database</div>
			<div id="msg-ct">
				<div id="msg">
					<div id="msg-inner">
						<p><tr:translation key="common.user"/>: <strong><%= user.getDescription() %></strong> | <a href="logout.jsp"><tr:translation key="common.logout"/></a></p>
						<p id="msg-inner-hidden">
							<tr:translation key="common.group"/>: <strong><%= defaultGroup.getDescription() %></strong> |
							<a href="management.jsp"><tr:translation key="management.description"/></a>
						</p>
					</div>
				</div>
			</div>
		</div>
		
		<div id="footer" class="cm_no_display">
			<div class="fl"><a href="http://www.cmdbuild.org" target="_blank">www.cmdbuild.org</a></div>
			<div id="cmdbuild_credits_link" class="fc"><tr:translation key="common.credits"/></div>
			<div class="fr"><a href="http://www.tecnoteca.com" target="_blank">Copyright &copy; Tecnoteca srl</a></div>
		</div>
	</body>
</html>