<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@include file="include.jsp" %>
<portlet:defineObjects />
<%@page errorPage = "error.jsp" %>
<a class="CMDBuildPortletURL" rel="cmdbuildPortletURL">Torna alla home</a>
<%
try{
%>
<%@include file="variables.jsp" %>
<%
            if (user != null && user.length() > 0) {
%>
<%@include file="javascript.jsp" %>
<div class="CMDBuildPortletContainer">
    <div id="CMDBuildCmdbheader"></div>
    <div id="CMDBuildContainer">
        <%@include file="advancecontainer.jsp" %>
    </div>
</div>
<% } else {%>
<div id="CMDBuildContainer">
<div id="CMDBuildError">
    Portlet momentaneamente non disponibile<br/>
</div>
<%
    Log.PORTLET.debug("Trusted service not defined in portlet.properties");
    }
} catch (NumberFormatException ex) {
    Log.PORTLET.warn("User not already connected");
} catch (Exception ex) {
    Log.PORTLET.warn("Generic error. See detailed message", ex);
}%>
 <div id="CMDBuildClear">&nbsp;</div>
</div>