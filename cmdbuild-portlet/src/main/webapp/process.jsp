<!-- Process section with tabs -->
<div id="CMDBuildCmdbuild_tabs" style="display: none">
    <ul>
        <% if(displayStartProcess) {%>
        <li><a id="CMDBuildShowprocessform" onclick="CMDBuildShowProcessForm()" class="CMDBuilSelectedtab">Scheda</a></li>
        <%} if (displayOpenedProcesses) {%>
        <li><a id="CMDBuildShowprocessgrid" onclick="CMDBuildShowProcessGrid()">Lista</a></li>
        <%}%>
    </ul>
</div>

<!-- Process editor -->
<div id="CMDBuildProcess" class="CMDBuildProcess" style="display: none">
    <div id="CMDBuildProcessdata">
        <div id="CMDBuildProcessformwrapper" class="CMDBuildProcessdata">&nbsp;</div>
        <div id="CMDBuildProcessresponse" title="Esito operazione"></div>
    </div>
</div>

<!-- Advance Process editor -->
<div id="CMDBuildAdvanceProcess" class="CMDBuildProcess" style="display: none">
    <div id="CMDBuildAdvanceProcessdata">
        <div id="CMDBuildAdvanceProcessformwrapper" class="CMDBuildProcessdata">&nbsp;</div>
        <div id="CMDBuildAdvanceProcessresponse" title="Esito operazione"></div>
    </div>
</div>
