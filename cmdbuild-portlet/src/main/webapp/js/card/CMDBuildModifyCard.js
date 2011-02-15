function CMDBuildModifyCard(classname, classdescription, cardid, privilege){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/CompiledFormServlet',
        data: {
            type: 'card',
            classname: classname,
            classdescription: classdescription,
            cardid: cardid,
            privilege: privilege
        },
        complete: function(response) {
            jQuery.unblockUI();
            var result = response.responseText;
            jQuery(".CMDBuildCurrentSelectedForm").hide();
            jQuery(".CMDBuildCurrentSelectedForm").removeClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildModify").addClass("CMDBuildCurrentSelectedForm");
            jQuery("#CMDBuildTablecontainer").hide("fast");
            jQuery("#CMDBuildEditform > *").remove();
            jQuery('#CMDBuildModifyform > *').remove();
            jQuery("#CMDBuildEditcardresponse > *").remove();
            jQuery('#CMDBuildModifyform').append(result);
            jQuery('#CMDBuildModify').show("fast");
        }
    });
}


