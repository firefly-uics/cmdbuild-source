function CMDBuildLinkCardsGrid(id) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var cqlFilter = jQuery("#filter_"+id).text();
    var defaultSelection = jQuery("#defaultSelection_"+id).text();
    var params;
    if (defaultSelection != "") {
        CMDBuildAddDefaultSelection(id, defaultSelection);
    }
    if (cqlFilter != "") {
        params = buildCQLQueryParameters(cqlFilter);
    }
    if (params == undefined) {
        params = new Object();
    }
    params.operation = "header";
    params.identifier = id;
    var colModel;
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/LinkCardServlet',
        data: params,
        complete: function(response) {
            colModel = eval(response.responseText);
            jQuery(".CMDBuildLinkCardGrid").flexigrid({
                url: baseURL+'/LinkCardServlet?operation=data&identifier='+id,
                dataType: 'xml',
                colModel :  colModel,
                searchitems : [{
                    display: 'Tutti',
                    name : 'description'
                }],
                usepager: true,
                useRp: true,
                rp: 10,
                singleSelect: true,
                showTableToggleBtn: true,
                striped:false,
                containerId: '.CMDBuildLinkCardGridContainer'
            });
            jQuery(".CMDBuildLinkCardGrid").flexReload();
            jQuery.unblockUI();
        }
    });
}

function CMDBuildAddDefaultSelection(id, cqlSelection) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var cqlparams = buildCQLQueryParameters(cqlSelection);
    if (cqlparams == undefined) {
        cqlparams = new Object();
    }
    cqlparams.operation = "defaultSelected";
    cqlparams.identifier = id;
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/LinkCardServlet',
        data: cqlparams,
        complete: function(response) {
            jQuery("#item_"+id+" > *").remove();
            var defaultSelectedValues = response.responseText;
            jQuery("#item_"+id).append(defaultSelectedValues);
            CMDBuildUpdateRequired(id);
        }
    });
}

function CMDBuildSelectLinkCard(component, id, description, type) {
    if (type == 'radio') {
        CMDBuildLinkCardRadioButton(component, id, description);
    } else if (type == 'checkbox') {
        CMDBuildLinkCardAddItem(component, id, description);
    }
}

function CMDBuildLinkCardRadioButton(component, id, description) {
    jQuery('.CMDBuildLinkCardItem').remove();
    CMDBuildLinkCardAddItem(component, id, description);
}

function CMDBuildLinkCardAddItem(component, id, description) {
    var gridRow = jQuery(component).parent().parent().parent();
    var iditem = '';
    //Tolgo la classe di selezione
    jQuery('.trSelected').removeClass('trSelected');
    jQuery(gridRow).addClass('trSelected');
    iditem = jQuery('.trSelected td:first-child').text();
    jQuery(component).addClass('CMDBuildDisabled');
    jQuery(component).unbind('click');
    CMDBuildAddWorkflowWidgetInSession(id, iditem, description);
}

function CMDBuildAddWorkflowWidgetInSession(id, iditem, description) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/WorkflowWidgetServlet',
        data: {
            id: iditem,
            action: 'add',
            CMDBuildIdentifier: id
        },
        complete: function() {
            var image = '<img src=\''+baseURL+'/css/images/cross.png\' class=\'CMDBuildLinkCardItemButton\' onclick=\"CMDBuildRemoveItem(this, \''+id+'\', \''+iditem+'\', \''+description + '\')\" />';
            var inputfield = '<input type=\'hidden\' name=\'hiddenLinkCard_'+iditem+'\' value=\''+iditem+'\'/>';
            var row = '<div class=\'row_' + iditem + ' CMDBuildLinkCardItem\'>'
            + image
            + '<div class=\'CMDBuildLinkCardItemDescription\'>' + description + '</div>'
            + inputfield
            + '</div>';
            jQuery('#item_'+id).append(row);
            CMDBuildUpdateRequired(id);
        }
    });
}

function CMDBuildRemoveItem(component, id, iditem, description) {
    var baseURL = CMDBuild.Runtime['BaseURL'];
    var gridButton = jQuery("linkCardButton_"+iditem);
    jQuery(gridButton).removeClass('CMDBuildDisabled');
    jQuery(gridButton).bind('click', function() {
        CMDBuildLinkCardAddItem(component, id, description);
    });
    jQuery.ajax({
        type: 'POST',
        url: baseURL+'/WorkflowWidgetServlet',
        data: {
            id: iditem,
            action: 'remove',
            CMDBuildIdentifier: id
        },
        complete: function() {
            jQuery(component).parent().remove();
            CMDBuildUpdateRequired(id);
        }
    });
}

function CMDBuildUpdateRequired(id) {
    var items = jQuery("#item_" + id).children().length;
    if (items ==  0) {
        jQuery('#'+id).removeClass('CMDBuildWWRequiredOK');
    } else {
        jQuery('#'+id).addClass('CMDBuildWWRequiredOK');
    }
}