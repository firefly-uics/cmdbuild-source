<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp  --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects />
<%@include file="include.jsp" %>
<script>
    jQuery(document).ready(function(){
        var cmdbuildcss = "<link href=\"/CMDBuildPortlet/css/cmdbuild.css\" rel=\"stylesheet\" type=\"text/css\" />";
        jQuery("body").parent().append(cmdbuildcss);
    });
</script>
<div id="help">
    <h2>CMDBuild 1.2.2 Portlet</h2><br />
    <span style="font-weight: bold;">Sito del progetto:</span><a href="http://www.cmdbuild.org" target="_blank">www.cmdbuild.org</a><br />
    <span style="font-weight: bold;">Credits: </span><a href="http://www.tecnoteca.com">Tecnoteca srl</a>, <a href="http://www.comune.udine.it">Comune di Udine</a>, <a href="http://www.cogitek.it">Cogitek srl</a><br />
    <span style="font-weight: bold;"><a href="http://www.tecnoteca.com" target="_blank">Copyright &copy; Tecnoteca srl</a></span>
</div>