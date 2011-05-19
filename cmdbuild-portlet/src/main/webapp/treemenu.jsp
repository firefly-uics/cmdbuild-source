<%@page import="org.cmdbuild.services.soap.MenuSchema"%>
<%@page import="org.cmdbuild.portlet.layout.MenuLayout"%>
<%@page import="org.cmdbuild.portlet.utils.JSPPageUtils"%>
<%@page import="org.cmdbuild.portlet.Log"%>
<%@page import="org.cmdbuild.portlet.ws.*"%>
<div id="CMDBuildLeftmenu">
<ul id="CMDBuildMenu" class="filetree">

    <%
        PortletLayout layout = new PortletLayout(client, userEmail, contextPath);
        PortletURL portletURL = renderResponse.createRenderURL();
        String process = "";
        MenuSchema processmenu = operation.getMenu();
        String CMDBuildGroup = operation.getGroup(processmenu);
        Log.PORTLET.debug("Connected user group: " + CMDBuildGroup);
        request.getSession().setAttribute("connectedCMDBuildUserGroup", CMDBuildGroup);
        if (processmenu != null){
            MenuLayout menu = new MenuLayout();
            process = menu.printTreeMenu(processmenu);
        }
        JSPPageUtils jspUtils = new JSPPageUtils();
        String customNode = jspUtils.getCustomTreeNodeHtml();
    %>
    <jsp:include page="<%= customNode %>" />
    <%= process%>
</ul>
</div>
<script type="text/javascript">
    if (jQuery("CMDBuildMenu").children().length > 0){
        jQuery(".isDefaultToDisplay").children().trigger("click");
    }

</script>