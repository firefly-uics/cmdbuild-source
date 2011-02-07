function CMDBuildShowNoteEditor(){
    //get the position of the placeholder element
    var pos = jQuery("#CMDBuildPopupPlaceholder").offset();
    //show the menu directly over the placeholder
    jQuery("#CMDBuildTexteditorcontainer").css( {
        "left": (pos.left) + "px",
        "top":pos.top + "px"
    } );
    jQuery('.wysiwyg').remove();
    jQuery('#CMDBuildTexteditor').wysiwyg();
    jQuery('#CMDBuildTexteditorcontainer').show();
}

function CMDBuildSaveNote(){
    CMDBuildShowWait();
    var body = jQuery("#CMDBuildTexteditorIFrame").document();
    var notes = jQuery(body).find('body').html();
    var note = '<input id="CMDBuildNote_hidden" type=\"hidden\" name=\"Notes\" />';
    jQuery(".CMDBuilProcessForm").append(note);
    jQuery("#CMDBuildNote_hidden").attr('value', notes);
    jQuery.unblockUI();
}