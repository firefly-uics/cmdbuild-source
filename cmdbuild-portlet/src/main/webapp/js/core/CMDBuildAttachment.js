function CMDBuildInitializeAttachmentGrid(classname, cardid){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var servleturl;
    if (classname != undefined && cardid != undefined){
        servleturl = baseURL+'/DisplayAttachmentServlet?classname='+classname+'&cardid='+cardid+'&deletebutton=false';
    } else {
        servleturl = baseURL+'/DisplayAttachmentServlet?deletebutton=true';
    }

    jQuery('.CMDBuildAttachmentGrid').flexigrid({
        height:'auto',
        url: servleturl,
        dataType: 'xml',
        containerId: '.CMDBuildProcessdata',
        colModel :  [
        {
            display:'File',
            name:'File',
            width:180,
            sortable:false
        },
        {
            display:'Categoria',
            name:'Categoria',
            width:180,
            sortable:false
        },
        {
            display:'Descrizione',
            name:'Descrizione',
            width:180,
            sortable:false
        },
        {
            display:'',
            name:'',
            fixed: true,
            width:100,
            sortable:false
        }],
        usepager: true,
        title: 'Allegati',
        singleSelect: true,
        striped:false
    });
}

function CMDBuildShowAttachmentList(classname, cardid) {
    jQuery("#CMDBuildFlowstatus").remove();
    jQuery("#CMDBuildTablecontainer > *").remove();
    jQuery("#CMDBuildTablecontainer").append("<table class=\"CMDBuildAttachmentGrid\"></table>");
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var servleturl;
    CMDBuildShowWait();
    if (classname != undefined && cardid != undefined){
        servleturl = baseURL+'/DisplayAttachmentServlet?classname='+classname+'&cardid='+cardid+'&deletebutton=false';
    } else {
        servleturl = baseURL+'/DisplayAttachmentServlet?deletebutton=true';
    }
    jQuery(".CMDBuildAttachmentGrid").flexigrid({
        height:'auto',
        url: servleturl,
        dataType: 'xml',
        containerId: '.CMDBuildProcessdata',
        colModel :  [{
            display:'File',
            name:'File',
            width:180,
            sortable:false
        },
        {
            display:'Categoria',
            name:'Categoria',
            width:180,
            sortable:false
        },
        {
            display:'Descrizione',
            name:'Descrizione',
            width:180,
            sortable:false
        },
        {
            display:'',
            name:'',
            fixed: true,
            width:100,
            sortable:false
        }],
        usepager: true,
        title: 'Allegati',
        singleSelect: true,
        striped:false
    });
    jQuery(".CMDBuildAttachmentGrid").click( function (e) {
        var filename = jQuery('.trSelected')[0].cells[0].textContent;
        CMDBuildDownloadAttachment(classname, cardid, filename);
    });
    jQuery.unblockUI();
}

function CMDBuildDeleteAttachment(filename, classname, cardid){
    var identifiers = jQuery(".CMDBuildIdentifier");
    var identifier;
    if (identifiers != undefined){
        for (var i=0; i < identifiers.length; i++){
            if (jQuery(identifiers[i] + ":visible")){
                identifier = jQuery(identifiers[i]);
            }
        }
    }
    var widgetId = jQuery(identifier).attr("value");

    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildShowWait();
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/WorkflowWidgetServlet',
        data: {
            CMDBuildAttachmentClassname: classname,
            filename: filename,
            CMDBuildAttachmentCardId: cardid,
            CMDBuildIdentifier: widgetId,
            action: "delete"
        },
        complete: function(response) {
            setTimeout("CMDBuildWorkflowWidgetResponse()", 1500);
        }
    });

}

function CMDBuildDownloadAttachment(classname, cardid, filename){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var urlservlet = baseURL+'/DownloadAttachmentServlet?classname='+classname+'&cardid='+cardid+'&filename='+filename;
    window.open(urlservlet, "_blank");
}
