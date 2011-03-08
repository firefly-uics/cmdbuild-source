<%
            String processname = request.getParameter("processname");
            String processid = request.getParameter("processid");
%>

<script type="text/javascript">
    jQuery(document).ready(function(){
        var url = document.location.href;
        var home = url.split('?')[0];
        //Clean URL
        jQuery('.CMDBuildPortletURL').attr('href', '');
        //Add new URL
        jQuery('.CMDBuildPortletURL').attr('href', home);
        CMDBuildAdvanceProcessFromLink('<%= processname%>','<%= processid%>');
    });
</script>

<div id="CMDBuildLeftmenu">&nbsp;</div>
<div id="CMDBuildContent">
    <!-- Advance Process editor -->
    <div id="CMDBuildAdvanceProcess" class="CMDBuildProcess">
        <div id="CMDBuildAdvanceProcessdata">
            <div id="CMDBuildAdvanceProcessformwrapper" class="CMDBuildProcessdata">&nbsp;</div>
            <div id="CMDBuildAdvanceProcessresponse" title="Esito operazione"></div>
        </div>
    </div>
</div>