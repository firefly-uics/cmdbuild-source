var CMDBuildCname;

jQuery(document).ready(function(){
    CMDBuildHideAll();
    var baseURL = CMDBuild.Runtime['BaseURL'];
    // --- Include custom CSS to header ---
    var aeroporto = "<link href=\""+baseURL+"/css/aeroporto.css\" rel=\"stylesheet\" type=\"text/css\" />";
    var cmdbuildcss = "<link href=\""+baseURL+"/css/cmdbuild.css\" rel=\"stylesheet\" type=\"text/css\" />";
    var date_inputcss = "<link href=\""+baseURL+"/css/date_input.css\" rel=\"stylesheet\" type=\"text/css\" />";
    var flexigridcss = "<link href=\""+baseURL+"/css/flexigrid.css\" rel=\"stylesheet\" type=\"text/css\" />";
    var wysiwygcss = "<link href=\""+baseURL+"/css/jquery.wysiwyg.css\" rel=\"stylesheet\" type=\"text/css\" />";
    jQuery("body").parent().append(cmdbuildcss);
    jQuery("body").parent().append(date_inputcss);
    jQuery("body").parent().append(flexigridcss);
    jQuery("body").parent().append(wysiwygcss);
    jQuery("body").parent().append(aeroporto);

    var CMDBuildAdvanceProcessForm = {
        target:        '#CMDBuildAdvanceProcessresponse',
        beforeSubmit:   CMDBuildShowWait,
        success:       CMDBuildShowAdvanceProcessResponse,
        url:       baseURL+'/AdvanceWorkflowServlet',
        type:      'post',
        resetForm: true
    }

    jQuery("#CMDBuildAdvanceProcessform").validate({
        submitHandler: function(form) {
            jQuery(".CMDBuildHiddenCombo").remove();
            jQuery(form).ajaxSubmit(CMDBuildAdvanceProcessForm);
            return false;
        }
    });

    var CMDBuildEditformOptions = {
        target:        '#CMDBuildEditcardresponse',
        beforeSubmit:   CMDBuildShowWait,
        success:       CMDBuildShowCardResponse,
        url:       baseURL+'/CardServlet',
        type:      'post',
        resetForm: true
    }

    jQuery("#CMDBuildEditform").validate({
        submitHandler: function(form) {
            jQuery(".CMDBuildHiddenCombo").remove();
            jQuery(form).ajaxSubmit(CMDBuildEditformOptions);
            return false;
        }
    });

    var CMDBuildModifyformOptions = {
        target:        '#CMDBuildModifyform',
        beforeSubmit:   CMDBuildShowWait,
        success:       CMDBuildShowModifiedCardResponse,
        url:       baseURL+'/ModifyCardServlet',
        type:      'post',
        resetForm: true
    }

    jQuery("#CMDBuildModifyform").validate({
        submitHandler: function(form) {
            jQuery(".CMDBuildHiddenCombo").remove();
            jQuery(form).ajaxSubmit(CMDBuildModifyformOptions);
            return false;
        }
    });

    jQuery("#CMDBuildMenu").treeview({
        animated: "fast",
        persist: "cookie",
        collapsed: false
    });

    jQuery(jQuery.date_input.initialize);
    //Open default class to display
    jQuery(".isDefaultToDisplay").children().trigger("click");

});

function CMDBuildCheckForm(form){
    if (jQuery(form).valid()){
        CMDBuildShowWait();
        form.submit();
    }
    return false;
}


function CMDBuildShowDateInput(){
    jQuery(".CMDBuildDate_input").date_input();
}

function CMDBuildShowHistory(classname, id, type){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildShowHistoryPopup();
    jQuery('#CMDBuildCardgridhistory').flexigrid({
        url: baseURL+'/ElementHistoryServlet?classname='+classname+'&cardid='+id+'&type='+type,
        dataType: 'xml',
        colModel :  [{
            display:'ID',
            name:'id',
            width:15,
            hide:true
        },
        {
            display:'Data inizio',
            name:'BeginDate',
            index:'BeginDate',
            width:CMDBuildGetColumnSize(30),
            sortable:true
        },
        {
            display:'Data fine',
            name:'EndDate',
            index:'EndDate',
            width:CMDBuildGetColumnSize(30),
            sortable:true
        },
        {
            display:'Descrizione',
            name:'Description',
            index:'Description',
            width:CMDBuildGetColumnSize(30),
            sortable:true
        },
        {
            display:'Dettaglio',
            name:'',
            width:50
        }],
        usepager: true,
        title: 'Storia',
        useRp: true,
        rp: 10,
        singleSelect: true,
        showTableToggleBtn: true
    });
}

function CMDBuildShowElementDetail(index, classname, cardid, type){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/ElementDetail',
        data: {
            index: index,
            classname: classname,
            cardid: cardid,
            type: type
        },
        complete: function(response) {
            CMDBuildShowPopupElementDetail(response.responseText);
        }
    });
}

function CMDBuildHideAllForm(){
    jQuery("#CMDBuildCmdbuild_tabs").hide();
    jQuery("#CMDBuildTablecontainer").hide();
    jQuery("#CMDBuildReference").hide();
    jQuery('#CMDBuildEditcard').hide();
    jQuery('#CMDBuildModify').hide();
    jQuery('#CMDBuildProcess').hide();
    jQuery("#CMDBuildPopupwindow").hide();
    jQuery("#CMDBuildHistorypopup").hide();
    jQuery("#CMDBuildDetailpopup").hide();
    jQuery("#CMDBuildTexteditorcontainer").hide();
    jQuery('#CMDBuildEmail').hide();
    jQuery("#CMDBuildCustom").hide();
    jQuery("#CMDBuildCustomemail").hide();
}

function CMDBuildCleanForm(type){

    CMDBuildCleanReportForm();
    if (type == "card"){
        CMDBuildCleanCardForm();
        jQuery("#CMDBuildReference > *").remove();
        jQuery("#CMDBuildTablecontainer").show("fast");
    } else if (type == "process" || type == "advance"){
        CMDBuildResetProcessForm();
    }
}

function CMDBuildCleanReportForm() {
    jQuery("#CMDBuildReportformwrapper > *").remove();
}

function CMDBuildCleanCardForm(){
    jQuery('#CMDBuildEditcard').hide("fast");
    jQuery("#CMDBuildEditform > *").remove();
    jQuery("#CMDBuildEditcardresponse > *").remove();
    jQuery('#CMDBuildModify').hide("fast");
    jQuery("#CMDBuildModifyform > *").remove();
    jQuery("#CMDBuildModifycardresponse > *").remove();
    jQuery('#CMDBuildEmail').hide("fast");
    jQuery('#CMDBuildCustom').hide("fast");
    jQuery("#CMDBuildCustomform > *").remove();
    jQuery("#CMDBuildCustomresponse > *").remove();
}

function CMDBuildShowResponse()  {
    jQuery.unblockUI();
    jQuery('#CMDBuildFormresponse').show('fast');
    var formoptions='<option value=\'none\' selected=\'selected\'>  </option>\n';
    jQuery("#CMDBuildReference_"+CMDBuildCname).html(formoptions);
    setTimeout(function(){
        jQuery('#CMDBuildFormresponse').hide('fast')
    }, 5000);
}

function CMDBuildShowWait()  {
    jQuery.blockUI({
        message: '<h1>Attendere prego ...</h1>',
        css: {
            border: 'none',
            padding: '15px',
            backgroundColor: '#000',
            '-webkit-border-radius': '10px',
            '-moz-border-radius': '10px',
            opacity: .5,
            color: '#fff'
        }
    });
}

function CMDBuildShowCardResponse(responseText)  {
    jQuery.unblockUI();
    jQuery.growlUI('',responseText);
    setTimeout(function(){
        CMDBuildCleanCardForm();
        jQuery("#CMDBuildTablecontainer").show();
        jQuery("#CMDBuildCardgrid").flexReload();
    }, 2500);
}

function CMDBuildShowModifiedCardResponse(responseText)  {
    jQuery.unblockUI();
    jQuery.growlUI('',responseText);
    setTimeout(function(){
        CMDBuildCleanCardForm();
        jQuery("#CMDBuildTablecontainer").show();
        jQuery("#CMDBuildCardgrid").flexReload();
    }, 2500);
}

function CMDBuildChangeCheckboxValue(element){
    var hidden_id = "CMDBuild"+element.id + "_hidden";
    if(element.checked){
        jQuery(element).attr("value", "true");
        jQuery("#"+hidden_id).remove();
    } else {
        var parent = jQuery(element).parent();
        jQuery("#"+hidden_id).remove();
        var input_string = "<input id=\""+hidden_id+"\" type=\"hidden\" value=\"false\" name=\""+element.name+"\"";
        jQuery(parent).append(input_string);
    }
}

function CMDBuildHideAll(){
    var multilevel = jQuery(".CMDBuildmultilevel");
    for (var i=0; i< multilevel.length; i++){
        var options = jQuery(multilevel[i]).children().filter("option");
        for (var j=0; j<options.length; j++){
            if (jQuery(options[j]).hasClass("0")){
                continue;
            } else {
                jQuery(options[j]).hide();
            }
        }
    }
}

function CMDBuildHideChildren(selectedid){
    var child = document.getElementById(selectedid+"_");
    while (child != null){
        jQuery(child).children().remove();
        jQuery(child).append("<option value=\"\" selected=\"selected\">  </option>");
        var id = jQuery(child).attr("id");
        id = id + "_";
        //Clean next select element
        child = document.getElementById(id+"_");
    }
}

function CMDBuildComboFilter(selectedid, value){
    var targetSelect = jQuery("#" + selectedid + "_");
    var children = jQuery("."+value);
    var clonedChildren = children.clone();
    jQuery(targetSelect).children().remove();
    jQuery(targetSelect).append("<option value=\"\" selected=\"selected\">  </option>");
    if (value != "") {
        jQuery(clonedChildren).appendTo(targetSelect);
    }
    CMDBuildHideChildren(selectedid + "_");
}

function CMDBuildSelectedReference(com) {
    var form = jQuery(".CMDBuildProcess");
    if (com=='Seleziona') {
        var gridid = jQuery(".trSelected td:eq(0)").text();
        var griddescription = jQuery(".trSelected td:eq(1)").text();
        var defaultgrid = "<option value=''>  </option>\n";
        var gridoptions = defaultgrid + '<option value="' + gridid + '" selected=\'selected\'>'+griddescription+'</option>\n'
        jQuery("#CMDBuildReference_"+CMDBuildCname+"").html(gridoptions);
        jQuery("#CMDBuildReference > *").remove();
        jQuery("#CMDBuildCmdbuild_tabs").show();
        jQuery(form).show();
    } else if(com=='Annulla'){
        jQuery("#CMDBuildReference > *").remove();
        jQuery("#CMDBuildCmdbuild_tabs").show();
        jQuery(form).show();
    }
    var formid = form.attr('id');
    if (formid == 'CMDBuildProcessform'){
        jQuery("#CMDBuildProcessbuttonbar").hide();
    } else if (formid == 'CMDBuildAdvanceProcessForm'){
        jQuery("#CMDBuildAdvanceProcessbuttonbar").hide();
    }
}

function CMDBuildShowReferenceGrid(classname){
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery(".CMDBuildProcess").hide();
    jQuery("#CMDBuildReference > *").remove();
    jQuery("#CMDBuildReference").append("<table class=\"CMDBuildReferencegrid\"></table>");
    jQuery("#CMDBuildReference").show("fast");
    CMDBuildCname = classname;
    jQuery(".CMDBuildReferencegrid").flexigrid({
        url: baseURL+'/ReferenceServlet?classname='+classname,
        dataType: 'xml',
        colModel : [{
            display: 'Id',
            name : 'Id',
            width : 50,
            sortable : true,
            align: 'left',
            hide: true
        },
        {
            display: 'Descrizione',
            name : 'Description',
            width : 300,
            sortable : true,
            align: 'left'
        }],
        buttons : [{
            name: 'Seleziona',
            bclass: 'add',
            onpress : CMDBuildSelectedReference
        },
        {
            name: 'Annulla',
            bclass: 'delete',
            onpress : CMDBuildSelectedReference
        }],
        searchitems : [{
            display: 'Descrizione',
            name : 'Description',
            isdefault: true
        }],
        singleSelect: true,
        usepager: true,
        title: 'Reference',
        useRp: true,
        rp: 10,
        showTableToggleBtn: true
    });

    jQuery.unblockUI();
}

function CMDBuildGetColumnSize(percent){
    screen_res = (jQuery("#CMDBuildContent").width());
    col = parseInt((percent*(screen_res/100)));
    if (percent != 100){
        return col-10;
    }else{
        return col;
    }
}

function CMDBuildShowHistoryPopup(){
    //get the position of the placeholder element
    var pos = jQuery("#CMDBuildPopupPlaceholder").offset();
    //show the menu directly over the placeholder
    jQuery("#CMDBuildHistorypopup").css( {
        "left": (pos.left) + "px",
        "top":pos.top + "px"
    } );

    jQuery("#CMDBuildHistory").append("<table id=\"CMDBuildCardgridhistory\"></table>");
    jQuery("#CMDBuildHistorypopup").draggable();
    jQuery('#CMDBuildHistorypopup').show();
}

function CMDBuildShowInfoPopup() {
    //get the position of the placeholder element
    var pos = jQuery("#CMDBuildPopupPlaceholder").offset();
    //show the menu directly over the placeholder
    jQuery("#CMDBuildPopupwindow").css( {
        "left": (pos.left) + "px",
        "top":pos.top + "px"
    } );
    ;
    jQuery("#CMDBuildPopupwindow").draggable();
    jQuery('#CMDBuildPopupwindow').show();
}

function CMDBuildShowPopupElementDetail(text){
    //get the position of the placeholder element
    var pos = jQuery("#CMDBuildPopupPlaceholder").offset();
    //show the menu directly over the placeholder
    jQuery("#CMDBuildDetailpopup").css( {
        "left": (pos.left) + "px",
        "top":pos.top + "px"
    } );
    jQuery('#CMDBuildDetail > *').remove();
    jQuery("#CMDBuildDetail").append(text);
    jQuery("#CMDBuildDetailpopup").draggable({
        cancel: '.CMDBuildDetailContentContainer'
    });
    jQuery("#CMDBuildDetailpopup").show();
}

function CMDBuildCloseInfoPopup() {
    jQuery('#CMDBuildPopupwindow').hide();
    jQuery("#CMDBuildPopuptext > *").remove();
}

function CMDBuildCloseHistoryPopup() {
    jQuery('#CMDBuildHistorypopup').hide();
    jQuery("#CMDBuildHistory > *").remove();
}

function CMDBuildCloseDetailPopup() {
    jQuery('#CMDBuildDetailpopup').hide();
    jQuery("#CMDBuildDetail > *").remove();
}

function CMDBuildCloseAdvanceProcessAttachmentPopup() {
    jQuery('#CMDBuildAdvanceProcessAttachmentContainer').hide();
}

function CMDBuildSetSessionVariables(classname, type, classdescription, privilege){

    completed = false;
    jQuery("#CMDBuildTablecontainer > *").remove();
    jQuery("#CMDBuildReferencegrid > *").remove();
    var baseURL = CMDBuild.Runtime['BaseURL'];
    CMDBuildHideAllForm();
    CMDBuildShowWait();
    if (type == "class"){
        CMDBuildCleanCardForm();
        CMDBuildCleanProcessForm();
        jQuery.ajax({
            type: 'POST',
            url: baseURL+'/GridHeaderServlet',
            data: 'classname='+classname,
            complete: function(response) {
                var result = eval(response.responseText);
                CMDBuildCreateCardGridPage(result, classname, classdescription, privilege);
                jQuery.unblockUI();
            }
        });
    } else if (type == "processclass"){
        var displayStartProcess = CMDBuild.Runtime['displayStartProcess'];
        if (displayStartProcess) {
            CMDBuildCleanCardForm();
            CMDBuildCleanProcessForm();
            CMDBuildEnableProcessForm("process", classname, classdescription, privilege);
        } else {
            CMDBuildShowProcessGrid(classname, classdescription, privilege);
        }
    }

    return false;
}

function CMDBuildTextCharLimit(textfield, limit, info){

    var text = textfield.value;
    var textlength = text.length;
    limit = limit - 1;
    if (textlength > limit){
        textfield.value = text.substr(0, limit);
        jQuery("#"+info).html("<span style=\"color: #ff0000\">Hai raggiunto il limite di caratteri a disposizione</span>");
        jQuery("#"+info).show();
        return false;
    } else {
        jQuery("#"+info).hide();
        return true;
    }
}

function CMDBuildResetField(fieldId){
    var selected = jQuery("."+fieldId).find('option:first')
    jQuery(selected).attr('selected', 'selected').parent('select');
}

// CMDBuildResetProcessForm function comes from jQuery Form Plugin version: 2.18 (06-JAN-2009)
// that is dual licensed under the MIT and GPL licenses:
// http://www.opensource.org/licenses/mit-license.php
// http://www.gnu.org/licenses/gpl.html

function CMDBuildResetProcessForm() {
    return this.each(function() {
        // guard against an input with the name of 'reset'
        // note that IE reports the reset function as an 'object'
        if (typeof this.reset == 'function' || (typeof this.reset == 'object' && !this.reset.nodeType))
            if (!jQuery(this).hasClass("CMDBuildHiddenCombo")){
                this.reset();
            }
    });
};

function CMDBuildSelectImage(component, lookupType, id) {
    var selected = jQuery(".CMDBuildImageSelected");
    jQuery(selected).removeClass("CMDBuildImageSelected");
    jQuery(".CMDBuild"+lookupType).val("");
    jQuery("#"+lookupType).val(id);
    jQuery(component).addClass("CMDBuildImageSelected");
}