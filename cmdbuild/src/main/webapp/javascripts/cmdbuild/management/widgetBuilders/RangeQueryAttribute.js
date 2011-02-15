/**
 * @class CMDBuild.WidgetBuilders.RangeQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 * */
var translation = CMDBuild.Translation.management.findfilter;

CMDBuild.WidgetBuilders.RangeQueryAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.RangeQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);

/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.getQueryOptions = function() {
	return [
	    ['equals',translation.equals],
		['null',translation.nullo],
		['different',translation.different],
		['major',translation.major],
		['minor',translation.minor],
		['between',translation.between]
    ];
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.buildFieldsetForFilter = function(fieldId,field,query,originalFieldName){
	var field2 = field.cloneConfig(); 
	field2.name += "_end";
	field2.hideLabel = true;
	field2.disable();

	var orPanel = new Ext.Panel({
    	width: 30,
    	style: {padding : '0', margin:'0 0 0 20px '},
    	items: [{html: 'or'}]
    });
	
	query.on('select',function(query, type, id){	
		if (type.data.id === "between") {
			field.enable();
			field2.enable();
		} else if (type.data.id === 'null') {
			field.disable();
			field2.disable();
		} else {
			field.enable();
			field2.disable();
		}
	});

	var removeFieldButton = new Ext.Button({
		iconCls: 'delete'
	});
	
	var fieldset = new Ext.form.FieldSet({
        frame: false,
        border: false,
        style: {padding: '0', margin: '0'},
   		bodyStyle: {padding: '1px 0 1px 0', margin: '0'},
        removeButton: removeFieldButton,
        fieldsetCategory: originalFieldName,
        queryCombo: query,
        hideMode: 'offsets',
        layout: 'hbox',            
        layoutConfig: {                
            pack:'start',
            align:'top'
        },
        defaults: {
    		margins:'0 5 0 0'
    	},
        items: [{
        	xtype: 'panel',
        	margins: '0 30 0 0',
        	items: [removeFieldButton]
        },query,field,field2,orPanel],
        getAttributeField: function(){
        	return field;
        },
        getQueryCombo: function(){
        	return query;
        },
        getOrPanel: function(){
        	return orPanel;
        }
    });	
           
   	return fieldset;
};