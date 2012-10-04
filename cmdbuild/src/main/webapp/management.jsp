<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib uri="/WEB-INF/tags/translations.tld" prefix="tr" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.cmdbuild.services.SessionVars"%>
<%@ page import="org.cmdbuild.services.auth.UserContext"%>
<%@ page import="org.cmdbuild.services.auth.Group"%>
<%@ page import="org.cmdbuild.services.auth.User"%>
<%@ page import="org.cmdbuild.services.FilterService"%>
<%@ page import="org.cmdbuild.config.GisProperties"%>

<%
	SessionVars sessionVars = new SessionVars();
	String lang = sessionVars.getLanguage();
	UserContext userCtx = sessionVars.getCurrentUserContext();
	User user = userCtx.getUser();
	Group defaultGroup = userCtx.getDefaultGroup();
	FilterService.clearFilters(null, null);
	String extVersion = "4.1.0";
%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>/resources/css/ext-all.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>-ux/css/MultiSelect.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-<%= extVersion %>-ux/css/portal.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/extensible-1.5.1/resources/css/extensible-all.css" />

		<link rel="icon" href="images/favicon.ico" />
		
		<%@ include file="libsJsFiles.jsp"%>
		
		<script type="text/javascript">
			Ext.ns('CMDBuild.Runtime'); // runtime configurations
			CMDBuild.Runtime.UserId = <%= user.getId() %>;
			CMDBuild.Runtime.Username = '<%= user.getName() %>';

			CMDBuild.Runtime.DefaultGroupId = <%= defaultGroup.getId() %>;
			CMDBuild.Runtime.DefaultGroupName = '<%= defaultGroup.getName() %>';
			CMDBuild.Runtime.IsAdministrator = <%= defaultGroup.isAdmin() %>;

<%	if (userCtx.getGroups().size() == 1) { %>
			CMDBuild.Runtime.LoginGroupId = <%= defaultGroup.getId() %>;
<%	} %>
			CMDBuild.Runtime.AllowsPasswordLogin = <%= userCtx.allowsPasswordLogin() %>;

			CMDBuild.Runtime.CanChangePassword = <%= userCtx.canChangePassword() %>;
<%
	if (defaultGroup.getStartingClass() != null) {
%>
			CMDBuild.Runtime.StartingClassId = <%= defaultGroup.getStartingClass().getId() %>;
<%
	}
%>

		</script>
		<script type="text/javascript" src="javascripts/cmdbuild/application.js"></script>
		<script type="text/javascript" src="services/json/utils/gettranslationobject"></script>

		<%@ include file="coreJsFiles.jsp"%>
		<%@ include file="managementJsFiles.jsp"%>
<!--
		<script type="text/javascript" src="javascripts/cmdbuild/cmdbuild-core.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/cmdbuild-management.js"></script>
-->

			 <%	
		GisProperties g =  GisProperties.getInstance();
		if (g.isEnabled()) {
			if (g.isServiceOn(GisProperties.GOOGLE)) {
				%>
				<script src='http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=g.getGoogleKey()%>'></script>
				<%
			}
			if (g.isServiceOn(GisProperties.YAHOO)) {
				%>
				<script src="http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=<%=g.getYahooKey()%>"></script>
				<%
			}
		%>

		<script type="text/javascript" src="javascripts/OpenLayers-2.12/OpenLayers.js"></script>

		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/CMMap.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/CMSelectFeatureController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/CMZoomAndMousePositionControl.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/MapLayer.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/LayerBuilder.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/GeoUtility.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/MapEditingWindow.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/MapBuilder.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/MapPanel.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/PopupController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMMapController.js"></script>

		<%}%>

		<script type="text/javascript">
			Ext.onReady(function() {
				CMDBuild.app.Management.init();
			});
		</script>

		<title>CMDBuild</title>
	</head>
	<body>
		<div id="header" class="cm_no_display">
			<a href="http://www.cmdbuild.org" target="_blank"><img alt="CMDBuild logo" src="images/logo.jpg" /></a>
			<div id="instance_name"></div>
			<div id="header_po">Open Source Configuration and Management Database</div>
			<!-- required to display the map-->
			<div id="map"> </div>
			<div id="msg-ct" class="msg-blue">
				<div id="msg">
					<div id="msg-inner">
						<p><tr:translation key="common.user"/>: <strong><%= user %></strong> | <a href="logout.jsp"><tr:translation key="common.logout"/></a></p>
						<p id="msg-inner-hidden">
							<tr:translation key="common.group"/>: <strong><%= defaultGroup.getDescription() %></strong>
							<% if (userCtx.privileges().isAdmin()) { %>
								| <a href="administration.jsp"><tr:translation key="administration.description"/></a>
							<% } %>
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