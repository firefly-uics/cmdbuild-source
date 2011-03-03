<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />
<%@ page isErrorPage = "true" %>
<div class="message">
Oops! Si è verificato un errore<br /><br />
<%= exception.getMessage()%>
</div>
