var CMDBuildStoreDefaultReportParameters = function(id, reportname, extension) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/ReportServlet',
        data: {
            id: id,
            extension: extension,
            reportname: reportname,
            action: 'store'
        },
        complete: function() {
            jQuery.unblockUI();
            var urlservlet = baseURL+'/ReportServlet?action=print';
            window.open(urlservlet, "_blank");
        }
    });
}

function CMDBuildInitializeReportWidget(){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var CMDBuildReportWidgetOptions = {
        beforeSubmit:   CMDBuildShowWait,
        success: function() {
            jQuery.unblockUI();
            var urlservlet = baseURL+'/ReportServlet?action=print';
            window.open(urlservlet, "_blank");
        },
        url:       baseURL+'/ReportServlet',
        data:       {
            action: 'store'
        },
        type:      'post'
    }
    var elements = jQuery(".CMDBuildReportWidgetForm");
    for (var i=0; i<elements.length; ++i) {
        jQuery(elements[i]).validate({
            submitHandler: function(form) {
                jQuery(form).ajaxSubmit(CMDBuildReportWidgetOptions);
                return false;
            }
        });
    }
}

function CMDBuildGetReportParameters(id, reportname, extension) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildCleanReportForm();
    var idReport = id;
    var reportnameReport = reportname;
    var extensionReport = extension;
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/ReportServlet',
        data: {
            id: id,
            extension: extension,
            reportname: reportname,
            action: 'parameters'
        },
        complete: function(response) {
            jQuery.unblockUI();
            var result = response.responseText;
            if (result.length > 0) {
                CMDBuildCleanCardForm();
                jQuery("#CMDBuildCmdbuild_tabs").hide();
                jQuery("#CMDBuildTablecontainer").hide();
                jQuery("#CMDBuildReportformwrapper").append(result);
                jQuery(".CMDBuildCurrentSelectedForm").hide();
                jQuery(".CMDBuildCurrentSelectedForm").removeClass("CMDBuildCurrentSelectedForm");
                jQuery("#CMDBuildReport").addClass("CMDBuildCurrentSelectedForm");

                var CMDBuildTabPanel = new ListAdapter({
                    tabListId: 'CMDBuildReportbuttonbar',
                    panelsContainerId: 'CMDBuildReportformwrapper',
                    //per il css, indica la classe da aggiungere al tab selezionato
                    classForSelectedTabItem: 'CMDBuildCurrentTabSelected',
                    //per il css, indica la classe da aggiungere settare ai tab non selezionati
                    classForDefaultTabItem: 'tabListItem'
                });
                CMDBuildInitializeReportForm(idReport, reportnameReport, extensionReport);
                CMDBuildEnableReportForm();
                jQuery(".CMDBuildCurrentSelectedForm").show();
            } else {
                CMDBuildStoreDefaultReportParameters(id, reportname, extension);
            }
        }
    });
}

function CMDBuildInitializeReportForm(id, reportname, extension){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery("#CMDBuildReportformwrapper").show();
    jQuery("#CMDBuildReportFormPanel").show();
    var idReport = id;
    var reportnameReport = reportname;
    var extensionReport = extension;
    //Enable form validity check
    var CMDBuildReportFormOptions = {
        success: function() {
            if (idReport != undefined && reportnameReport != undefined && extensionReport != undefined ) {
                var urlservlet = baseURL+'/ReportServlet?id='+idReport+'&extension='+extensionReport+'&reportname='+reportnameReport+'&action=print';
                window.open(urlservlet, "_blank");
            }
        },
        url:       baseURL+'/ReportServlet',
        data:       {
            action: 'store'
        },
        type:      'post',
        resetForm: true
    };

    jQuery("#CMDBuildReportform").validate({
        submitHandler: function(form) {
            jQuery(form).ajaxSubmit(CMDBuildReportFormOptions);
            return false;
        }
    });
}

function CMDBuildCleanReportForm() {
    jQuery("#CMDBuildReportdata").hide();
    jQuery("#CMDBuildReportformwrapper > *").remove();
    jQuery("#CMDBuildReportformwrapper").text('');
    jQuery("#CMDBuildReportformwrapper").hide();
}

function CMDBuildEnableReportForm() {
    jQuery("#CMDBuildReportdata").show();
    jQuery("#CMDBuildReportformwrapper").show();
}


function CMDBuildPrintReport(id, reportname, extension) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    if (id != undefined && reportname != undefined && extension != undefined ) {
        var urlservlet = baseURL+'/ReportServlet?id='+id+'&extension='+extension+'&reportname='+reportname+'&action=print';
        window.open(urlservlet, "_blank");
    }
}


