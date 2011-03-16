function CMDBuildEnableEditCard(type, classname, classdescription, privilege){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/AddCardLayoutServlet',
        data: {
            type: type,
            classname: classname,
            classdescription: classdescription,
            privilege: privilege
        },
        complete: function(response) {
            jQuery.unblockUI();
            var result = response.responseText;
            jQuery(".CMDBuildCurrentSelectedForm").hide();
            jQuery(".CMDBuildCurrentSelectedForm").removeClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildEditcard").addClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildTablecontainer").hide();
            jQuery("#CMDBuildModifyform > *").remove();
            jQuery("#CMDBuildEditform > *").remove();
            jQuery("#CMDBuildModifycardresponse > *").remove();
            jQuery('#CMDBuildEditform').append(result);
            jQuery('#CMDBuildEditcard').show();
        }
    });
}

function CMDBuildCreateCardGridPage(response, classname, classdescription, privilege){
    jQuery('#CMDBuildEditcard').hide("fast");
    jQuery("#CMDBuildEditform > *").remove();
    jQuery("#CMDBuildEditcardresponse > *").remove();
    jQuery('#CMDBuildProcess').hide("fast");
    jQuery("#CMDBuildProcessFormContainer > *").remove();
    jQuery("#CMDBuildProcessresponse > *").remove();
    jQuery("#CMDBuildTablecontainer > *").remove();
    jQuery("#CMDBuildReferencegrid > *").remove();
    jQuery("#CMDBuildModify").hide("fast");
    jQuery("#CMDBuildModifyform > *").remove();
    jQuery("#CMDBuildModifycardresponse > *").remove();
    //Creo la tabella in cui inserire la flexigrid
    jQuery("#CMDBuildTablecontainer").append("<table id=\"CMDBuildCardgrid\"></table>");
    jQuery("#CMDBuildTablecontainer").show("fast");
    var buttons;
    var baseURL = CMDBuild.Runtime['BaseURL'];
    if (privilege == "WRITE") {
        buttons = [
        {
            name: 'Nuovo',
            bclass: 'add',
            onpress : function(){
                CMDBuildShowWait();
                CMDBuildEnableEditCard("card", classname, classdescription, privilege);
            }
        },
        {
            name: 'Modifica',
            bclass: 'modify',
            onpress : function(){
                CMDBuildShowWait();
                var cardid = jQuery(".trSelected td:eq(0)").text();
                if (cardid.length > 0){
                    CMDBuildModifyCard(classname, classdescription, cardid, privilege);
                } else {
                    jQuery.unblockUI();
                    alert("Non è stato selezionato nessun valore da modificare");
                }

            }
        },
        {
            separator: true
        }
        ];
    }
    jQuery("#CMDBuildCardgrid").flexigrid({
        url: baseURL+'/XMLServlet?classname='+classname+'&type=class',
        dataType: 'xml',
        colModel :  response,
        buttons : buttons,
        searchitems : [{
            display: 'Tutti',
            name : 'description'
        }],
        usepager: true,
        title: classdescription,
        useRp: true,
        rp: 10,
        singleSelect: true,
        showTableToggleBtn: true
    });
}