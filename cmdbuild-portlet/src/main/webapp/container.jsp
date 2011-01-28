<div id="CMDBuildContent">
    <div id="CMDBuildPopupPlaceholder">&nbsp;</div>
    <%@include file="process.jsp" %>
    <%@include file="grid.jsp" %>
    <%@include file="card.jsp" %>
    <%@include file="report.jsp" %>
    <%@include file="popup.jsp" %>
    <%
        String[] customSections = jspUtils.getCustomSectionHtml();
        if (customSections != null) {
            for (int i=0; i<customSections.length; i++) {
                String section = customSections[i];
    %>
        <jsp:include page="<%= section %>" />
    <%
            }
        }
    %>
</div>