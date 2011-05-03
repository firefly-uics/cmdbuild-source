<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.cmdbuild.services.SessionVars"%>
<%@ page import="org.cmdbuild.services.auth.UserContext"%>
<%@ page import="org.cmdbuild.services.auth.Group"%>
<%@ page import="org.cmdbuild.services.auth.User"%>
<%@ page import="org.cmdbuild.servlets.json.Login"%>
<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%
	SessionVars sessionVars = new SessionVars();
	String lang = sessionVars.getLanguage();
	UserContext userCtx = sessionVars.getCurrentUserContext();
	String extVersion = "3.3";
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />	
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all.css" />
		<link rel="icon" href="images/favicon.ico" />
	
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/adapter/ext/ext-base.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/ext-all.js"></script>
	
		<script type="text/javascript" src="javascripts/cmdbuild/form/FormOverride.js"></script>
	
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/plugins/IconCombo.js"></script>
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>-ux/Notification.js"></script>
	
		<!-- 1. Main script -->
		<script type="text/javascript">
			Ext.BLANK_IMAGE_URL = 'javascripts/ext-<%= extVersion %>/resources/images/default/s.gif';
		</script>
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/application.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Ajax.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/Msg.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/core/PopupWindow.js"></script>	
		<script type="text/javascript" src="javascripts/cmdbuild/core/serviceProxy/ServiceProxy.js"></script>
		
		<!-- 2. Translations -->
		<script type="text/javascript" src="javascripts/ext-<%= extVersion %>/locale/ext-lang-<%= lang %>.js"></script>
		<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>
	
		<script type="text/javascript">
			Ext.ns('CMDBuild.Runtime'); // runtime configurations
	<% if (userCtx != null) { 
			User user = userCtx.getUser();
	%>
			CMDBuild.Runtime.Username = '<%= user.getName() %>';
			CMDBuild.Runtime.Groups = <%= Login.serializeGroupForLogin(userCtx.getGroups()) %>;
	<% } %>	
		</script>
		<!-- 3. Login script -->
		<script type="text/javascript" src="javascripts/cmdbuild/login.js"></script>
		<title>CMDBuild</title>
	</head>
	<body>
		<div id="header">
			<img alt="CMDBuild logo" src="images/logo.jpg" />
			<div id="header_po">Open Source Configuration and Management Database</div>
		</div>
		<div id="login_box_wrap">
			<div id="login_box"></div>
		</div>
		<div id="release_box" class="x-panel-header" style="background: none; border: none; text-align: center;"><span class="x-panel-header-text">CMDBuild <tr:translation key="release"/></span></div>
	</body>
</html>