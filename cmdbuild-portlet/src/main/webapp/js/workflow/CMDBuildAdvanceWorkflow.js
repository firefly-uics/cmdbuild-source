function CMDBuildAdvanceProcess(classname, cardid){
    var email = CMDBuild.Runtime['email'];
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var description = this.CMDBuildProcessclassdescription;
    jQuery("#CMDBuildShowprocessgrid").removeClass("CMDBuilSelectedtab");
    jQuery("#CMDBuildShowprocessform").addClass("CMDBuilSelectedtab");
    jQuery("#CMDBuildShowprocessform").removeAttr('onclick');
    jQuery("#CMDBuildShowprocessform").unbind('click');
    jQuery("#CMDBuildShowprocessform").css('cursor', 'default');
    jQuery("#CMDBuildFlowstatus").remove();
    CMDBuildShowWait();
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/CompiledFormServlet',
        data: {
            type: 'advance',
            classname: classname,
            cardid: cardid,
            email: email,
            classdescription: description,
            privilege: CMDBuildProcessprivilege
        },
        complete: function(response) {
            jQuery.unblockUI();
            CMDBuildCleanProcessForm();
            var result = response.responseText;
            jQuery(".CMDBuildCurrentSelectedForm").hide();
            jQuery(".CMDBuildCurrentSelectedForm").removeClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildAdvanceProcess").addClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildCmdbuild_tabs").hide();
            jQuery("#CMDBuildTablecontainer").hide();
            jQuery('#CMDBuildAdvanceProcessformwrapper').append(result);
            CMDBuildEnableWrapper("CMDBuildAdvanceProcess");
            CMDBuildInitializeAdvanceProcessForm();
            CMDBuildAddButtonBar(classname, cardid, "CMDBuildAdvanceProcess", true);
            CMDBuildInitializeWorkflowWidget();
            CMDBuildInitializeReportWidget();
            CMDBuildInitializeAttachmentGrid(classname, cardid);
            jQuery("#CMDBuildTexteditor").wysiwyg();
            jQuery("#CMDBuildAdvanceProcessdata").show();
        }
    });
}

function CMDBuildInitializeAdvanceProcessForm(){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var CMDBuildAdvanceProcessformOptions = {
        target: '#CMDBuildAdvanceProcessresponse',
        beforeSubmit:   CMDBuildChekRequiredWorkflowWidget,
        success:       CMDBuildShowAdvanceProcessResponse,
        url:       baseURL+'/AdvanceWorkflowServlet',
        type:      'post',
        resetForm: true
    };
    jQuery("#CMDBuildAdvanceProcessform").validate({
        submitHandler: function(form) {
            jQuery(form).ajaxSubmit(CMDBuildAdvanceProcessformOptions);
            return false;
        }
    });
}

function CMDBuildShowAdvanceProcessResponse()  {
    jQuery(".CMDBuildHiddenCombo").remove();
    jQuery.unblockUI();
    jQuery("#CMDBuildAdvanceProcessresponse").dialog({
        bgiframe: true,
        modal: true,
        height: 100,
        buttons: {
            Ok: function() {
                CMDBuildShowProcessGrid();
                jQuery(this).dialog('close');
            }
        }
    });
}

function CMDBuildAdvanceProcessFromLink(classname, cardid){
    var email = CMDBuild.Runtime['email'];
    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildShowWait();
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/CompiledFormServlet',
        data: {
            type: 'advance',
            classname: classname,
            cardid: cardid,
            email: email
        },
        complete: function(response) {
            jQuery.unblockUI();
            CMDBuildCleanProcessForm();
            var result = response.responseText;
            jQuery('#CMDBuildAdvanceProcessformwrapper').append(result);
            CMDBuildInitializeAdvanceProcessFormFromLink();
            CMDBuildAddButtonBar(classname, cardid, "CMDBuildAdvanceProcess", true);
            CMDBuildInitializeWorkflowWidget();
            CMDBuildInitializeReportWidget();
            CMDBuildInitializeAttachmentGrid(classname, cardid);
            CMDBuildEnableWrapper("CMDBuildAdvanceProcess");
            jQuery("#CMDBuildTexteditor").wysiwyg();
            jQuery("#CMDBuildAdvanceProcessdata").show();
        }
    });
}

function CMDBuildInitializeAdvanceProcessFormFromLink(){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var CMDBuildAdvanceProcessformOptions = {
        target: '#CMDBuildAdvanceProcessresponse',
        beforeSubmit:   CMDBuildShowWait,
        success:       CMDBuildShowAdvanceProcessResponseFromLink,
        url:       baseURL+'/AdvanceWorkflowServlet',
        type:      'post',
        resetForm: true
    };

    jQuery("#CMDBuildAdvanceProcessform").validate({
        submitHandler: function(form) {
            jQuery(".CMDBuildHiddenCombo").remove();
            jQuery(form).ajaxSubmit(CMDBuildAdvanceProcessformOptions);
            return false;
        }
    });
}

function CMDBuildShowAdvanceProcessResponseFromLink()  {
    jQuery.unblockUI();
    jQuery("#CMDBuildAdvanceProcessresponse").dialog({
        bgiframe: true,
        modal: true,
        height: 100,
        buttons: {
            Ok: function() {
                jQuery(this).dialog('close');
                jQuery("#CMDBuildAdvanceProcessresponse > *").remove();
                var url = jQuery(".CMDBuildPortletURL").attr("href");
                window.location.href=url;
            }
        }
    });
}