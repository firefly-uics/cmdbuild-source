function CMDBuildInitializeWorkflowWidget(){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    //Empty LinkCard items
    jQuery('.CMDBuildLinkCardItem').remove();

    var CMDBuildWorkflowWidgetOptions = {
        beforeSubmit:   CMDBuildShowWait,
        success:       CMDBuildWorkflowWidgetResponse,
        url:       baseURL+'/WorkflowWidgetServlet',
        type:      'post',
        resetForm: true
    }

    var elements = jQuery(".CMDBuildWorkflowWidgetForm");
    for (var j=0; j<elements.length; ++j) {
        jQuery(elements[j]).validate({
            submitHandler: function(form) {
                jQuery(form).ajaxSubmit(CMDBuildWorkflowWidgetOptions);
                return false;
            }
        });
    }
}

function CMDBuildWorkflowWidgetResponse(){
    CMDBuildRequiredFields--;
    jQuery.unblockUI();
    jQuery('.CMDBuildAttachmentGrid').flexReload();
}
