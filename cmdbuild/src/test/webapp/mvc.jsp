<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%	
String extVersion = "3.3"; 
String lang = "it";
%>	
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="stylesheets/cmdbuild.css" />
		<link rel="stylesheet" type="text/css" href="javascripts/ext-3.3/resources/css/ext-all.css" />
		<link rel="icon" href="images/favicon.ico" />
		
		<!-- Libraries -->
		<%@ include file="libsJsFiles.jsp"%>
		<script type="text/javascript" src="javascripts/log/log4javascript.js"></script>
		
		<!-- Test webapp -->
		<script type="text/javascript" src="javascripts/tools/LoggerPanel.js"></script>
		<script type="text/javascript" src="javascripts/mvc/view.js"></script>
		<script type="text/javascript" src="javascripts/mvc/model.js"></script>
		
		<script type="text/javascript">
			Ext.onReady(function() {
				var m = new MVCModel();
				var v = new MVCView(m);
			});
		</script>
		<title>Test MVC architecture</title>
		
	</head>
	<body>
		
	</body>
</html>