//Default process flow status
var CMDBuildFlowstatus = "open.running";
var CMDBuildProcesstype;
var CMDBuildProcessclassname;
var CMDBuildProcessclassdescription;
var CMDBuildProcessprivilege;

function CMDBuildSetFlowstatus(classname, status){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery("#CMDBuildProcessgrid").flexOptions({
        url: baseURL+'/XMLServlet?classname='+classname+'&type=processclass&flowstatus='+status
    });
    jQuery("#CMDBuildProcessgrid").flexReload();
}

function CMDBuildAddButtonBar(classname, cardid, container, advanceProcess){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/ButtonBarServlet',
        data: {
            classname: classname,
            advanceProcess: advanceProcess,
            cardId: cardid
        },
        complete: function(response) {
            var result = response.responseText;
            jQuery("#"+container).append(result);
            jQuery('#CMDBuildCmdbuild_tabs').show();
            jQuery("#"+container+"wrapper").show();
            jQuery("#"+container).show();
            CMDBuildGetProcessHelp(container);
            jQuery('.linkCards').click(function() {
                CMDBuildShowWait();
                CMDBuildLinkCardsGrid(jQuery(this).attr('id'));
            });
        }
    });
}

function CMDBuildGetProcessHelp(container){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/ProcessHelpServlet',
        data: 'classname='+CMDBuildProcessclassname,
        complete: function(response) {
            jQuery("#"+container+"Help").text('');
            var result = response.responseText;
            jQuery("#"+container+"Help").append(result);
            if (jQuery("#"+container+'buttonbar') != undefined & jQuery("#"+container+"formwrapper > *").length > 0) {
                var CMDBuildTabPanel = new ListAdapter({
                    tabListId: container+'buttonbar',
                    panelsContainerId: container+'formwrapper',
                    //per il css, indica la classe da aggiungere al tab selezionato
                    classForSelectedTabItem: 'CMDBuildCurrentTabSelected',
                    //per il css, indica la classe da aggiungere settare ai tab non selezionati
                    classForDefaultTabItem: 'tabListItem'
                });
            }
        }
    });
}

function CMDBuildEnableWrapper(wrapper){
    jQuery("#"+wrapper+"formwrapper").show();
}

function CMDBuildShowProcessForm(){
    CMDBuildShowWait();
    CMDBuildCleanProcessForm();
    jQuery(".CMDBuildTexteditor").wysiwyg();
    CMDBuildEnableProcessForm(CMDBuildProcesstype, CMDBuildProcessclassname, CMDBuildProcessclassdescription, CMDBuildProcessprivilege);
    CMDBuild.Runtime.AttachmentCounter = 0;
    return false;
}

function CMDBuildShowProcessGrid(classname, classdescription, privilege){
    if (classname == undefined) {
        classname = CMDBuildProcessclassname;
    }
    if (classdescription == undefined) {
        classdescription = CMDBuildProcessclassdescription;
    }
    if (privilege == undefined) {
        privilege = CMDBuildProcessprivilege;
    }
    jQuery("#CMDBuildShowprocessgrid").addClass("CMDBuilSelectedtab");
    jQuery("#CMDBuildShowprocessform").removeClass("CMDBuilSelectedtab");
    jQuery("#CMDBuildShowprocessform").bind('click', function () {
        CMDBuildShowProcessForm();
    });
    jQuery("#CMDBuildShowprocessform").css('cursor', 'pointer');
    jQuery("#CMDBuildFlowstatus").remove();
    CMDBuildCleanProcessForm();
    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildShowWait();
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/GridHeaderServlet',
        data: {
            classname: classname,
            classdescription: classdescription,
            type: 'process'
        },
        complete: function(response) {
            var result = eval(response.responseText);
            CMDBuildAddProcessStatus(result, classname, classdescription, privilege);
        }
    });
    return false;
}

function CMDBuildCreateProcessGrid(colModel, classname, classdescription, privilege){
    jQuery('#CMDBuildProcessform').hide("fast");
    //Nascondo form di modifica dati e pulisco tutti i figli
    jQuery("#CMDBuildTablecontainer").append("<table id=\"CMDBuildProcessgrid\"></table>");
    jQuery("#CMDBuildTablecontainer").show("fast");
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery("#CMDBuildProcessgrid").flexigrid({
        url: baseURL+'/XMLServlet?classname='+classname+'&type=processclass&flowstatus='+CMDBuildFlowstatus,
        dataType: 'xml',
        colModel :  colModel,
        searchitems : [{
            display: 'Tutti',
            name : 'description'
        }],
        usepager: true,
        title: classdescription,
        useRp: true,
        rp: 10,
        singleSelect: true,
        showTableToggleBtn: true,
        striped:false,
        onSuccess: CMDBuildCallbacks.flexigridProcessCallback
    });
    jQuery.unblockUI();
}

function CMDBuildAddProcessStatus(colModel, classname, classdescription, privilege){
    jQuery("#CMDBuildEditcard").hide("fast");
    jQuery("#CMDBuildEditform > *").remove();
    jQuery("#CMDBuildEditcardresponse > *").remove();
    jQuery("#CMDBuildModify").hide("fast");
    jQuery('#CMDBuildModifyform > *').remove();
    jQuery('#CMDBuildModifycardresponse').remove();
    jQuery("#CMDBuildTablecontainer > *").remove();
    jQuery("#CMDBuildFlowstatus").remove();
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/AddProcessFlowStatus',
        data: {
            classname: classname,
            flowstatus: CMDBuildFlowstatus
        },
        complete: function(response) {
            var result = response.responseText;
            jQuery('#CMDBuildCmdbuild_tabs > ul').append(result);
            CMDBuildCreateProcessGrid(colModel, classname, classdescription, privilege);
        }
    });
}

function CMDBuildCleanProcessForm(){
    jQuery("#CMDBuildProcessdata").hide();
    jQuery(".CMDBuildProcessbuttonbarContainer").remove();
    jQuery("#CMDBuildProcessformwrapper > *").remove();
    jQuery("#CMDBuildProcessformwrapper").text('');
    jQuery("#CMDBuildProcessformwrapper").hide();
    jQuery("#CMDBuildAdvanceProcessdata").hide();
    jQuery("#CMDBuildReference > *").remove();
    jQuery(".CMDBuildProcessbuttonbarContainer").remove();
    jQuery("#CMDBuildAdvanceProcessformwrapper > *").remove();
    jQuery("#CMDBuildAdvanceProcessformwrapper").text('');
    jQuery("#CMDBuildAdvanceProcessformwrapper").hide();
}

function CMDBuildChekRequiredWorkflowWidget() { 
    if (jQuery(".CMDBuildWWRequired").length != jQuery('.CMDBuildWWRequiredOK').length) {
        return false;
    }
    CMDBuildShowWait();
}