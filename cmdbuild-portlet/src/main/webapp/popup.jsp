<!-- Information popup -->
    <div id="CMDBuildPopupwindow" style="display: none;">
        <div class="CMDBuildPopupTopBanner"></div>
        <div>
            <div class="CMDBuildDetailContentContainer" >
                <div id="CMDBuildPopuptext"></div><br />
            </div>
            <input id="CMDBuildClosepopup" type="button" value="Chiudi" onclick="CMDBuildCloseInfoPopup();"/>
        </div>
    </div>

<!-- History popup -->
<div id="CMDBuildHistorypopup" style="display: none">
    <div id="CMDBuildHistory" class="CMDBuildHistoryContainer"></div>
    <input id="CMDBuildClosehistorypopup" type="button" value="Chiudi" onclick="CMDBuildCloseHistoryPopup();"/>
</div>

<!-- Detail popup -->
<div id="CMDBuildDetailpopup" style="display: none;">
    <div class="CMDBuildPopupTopBanner">Dettaglio</div>
    <div>
        <div class="CMDBuildDetailContentContainer" >
            <div id="CMDBuildDetail" ></div>
        </div>
        <input id="CMDBuildClosedetailpopup" type="button" value="Chiudi" onclick="CMDBuildCloseDetailPopup();"/>
    </div>
</div>
<!-- Email editor -->
<div id="CMDBuildEmail" style="display:none;">
    <form id="CMDBuildEmailform">
        <p id="CMDBuildEmailsubject">
            <div class="CMDBuildRow"><label class="CMDBuildEmailtitle">To: </label><input type="text" name="to" class="CMDBuildCol2" style="width: 80%"/><img src="/CMDBuildPortlet/css/images/information.png" alt="Informazioni sul campo" onclick="CMDBuildShowEmailFieldInfo()"/></div>
            <div class="CMDBuildRow"><label class="CMDBuildEmailtitle">CC: </label><input type="text" name="cc" class="CMDBuildCol2" style="width: 80%"/></div>
            <div class="CMDBuildRow"><label class="CMDBuildEmailtitle">Oggetto: </label><input type="text" name="subject" class="CMDBuildCol2" style="width: 80%"/></div>
        </p>
        <p id="CMDBuildEmailcontentarea">
            <textarea id="CMDBuildEmailcontent"></textarea>
        </p>
        <input type="button" value="Invia segnalazione" onclick="CMDBuildSendEmail()"/>
        <input type="reset" value="Annulla" onclick="CMDBuildCloseEmail()"/>
    </form>
    <!-- Email reponse -->
    <div id="CMDBuildEmailresponse" style="display: none">
        <div class="CMDBuildPopupTopBanner"></div>
        <div>
            <div class="CMDBuildDetailContentContainer" >
                <div id="CMDBuildEmailResponseText"></div><br />
                <input id="CMDBuildCloseEmailpopup" type="button" value="Chiudi" onclick="CMDBuildCloseInfoPopup();"/>
            </div>
        </div>
    </div>
    <!-- Email info popup -->
    <div id="CMDBuildEmailfieldinfo" style="display:none;">Per inviare il messaggio a più destinatari utilizzare il carattere virgola (,) per separare un indirizzo dall'altro</div>
</div>
