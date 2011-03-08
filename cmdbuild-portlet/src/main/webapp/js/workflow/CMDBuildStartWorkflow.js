function CMDBuildEnableProcessForm(type, classname, classdescription, privilege){

    this.CMDBuildProcesstype = type;
    this.CMDBuildProcessclassname = classname;
    this.CMDBuildProcessclassdescription = classdescription;
    this.CMDBuildProcessprivilege = privilege;
    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildCleanProcessForm();
    CMDBuildEnableWrapper("CMDBuildProcess");
    jQuery("#CMDBuildShowprocessgrid").removeClass("CMDBuilSelectedtab");
    jQuery("#CMDBuildShowprocessform").addClass("CMDBuilSelectedtab");
    jQuery("#CMDBuildShowprocessform").removeAttr('onclick');
    jQuery("#CMDBuildShowprocessform").unbind('click');
    jQuery("#CMDBuildShowprocessform").css('cursor', 'default');
    jQuery("#CMDBuildFlowstatus").remove();
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
            CMDBuildCleanCardForm();
            jQuery(".CMDBuildCurrentSelectedForm").hide();
            jQuery(".CMDBuildCurrentSelectedForm").removeClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildProcess").addClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildCmdbuild_tabs").hide();
            jQuery("#CMDBuildTablecontainer").hide();
            jQuery('#CMDBuildProcessformwrapper').append(result);
            CMDBuildInitializeProcessForm();
            CMDBuildAddButtonBar(classname, -1, "CMDBuildProcess", false);
            CMDBuildInitializeWorkflowWidget();
            CMDBuildInitializeAttachmentGrid();
            jQuery("#CMDBuildProcessdata").show();
            jQuery("#CMDBuildTexteditor").wysiwyg();
        }
    });
}

function CMDBuildInitializeProcessForm(){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    //Enable form validity check
    var CMDBuildProcessFormOptions = {
        target: '#CMDBuildProcessresponse',
        beforeSubmit:   CMDBuildChekRequiredWorkflowWidget,
        success:       CMDBuildShowProcessResponse,
        url:       baseURL+'/StartWorkflowServlet',
        type:      'post',
        resetForm: true
    };

    jQuery("#CMDBuildProcessform").validate({
        submitHandler: function(form) {
            jQuery(form).ajaxSubmit(CMDBuildProcessFormOptions);
            return false;
        }
    });
}

function CMDBuildShowProcessResponse()  {
    //Cleaning attachment form container
    jQuery('#CMDBuildAttachmentFormContainer').children().remove();
    jQuery('#CMDBuildNote_hidden').remove();
    jQuery(".CMDBuildHiddenCombo").remove();
    CMDBuildShowProcessForm();
    jQuery.unblockUI();
    jQuery("#CMDBuildProcessresponse").dialog({
        bgiframe: true,
        modal: true,
        height: 100,
        buttons: {
            Ok: function() {
                jQuery('#CMDBuildProcessresponse > *').remove();
                jQuery(this).dialog('close');
            }
        }
    });
}
