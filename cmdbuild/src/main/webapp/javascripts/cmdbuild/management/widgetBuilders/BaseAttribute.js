/**
 * @Class CMDBuild.WidgetBuilders.BaseAttribute
 * Abstract class to define the interface of the CMDBuild attributes
 **/
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.BaseAttribute = function () {};

CMDBuild.WidgetBuilders.BaseAttribute.prototype = {
	/**
	 * This template method return a combo-box with the options given
	 * by the getQueryOptions method that must be implemented in the subclasses
	 * @param attribute
	 * @return Ext.form.ComboBox
	 */
	getQueryCombo: function(attribute) {	
		var store = new Ext.data.SimpleStore({
			fields: ['id','type'],
			data: this.getQueryOptions()
		});
	
		return new Ext.form.ComboBox({
			labelWidth: CMDBuild.CM_LABEL_WIDTH,
			fieldLabel: attribute.description,
			labelSeparator: "",
			hideLabel: true,
			name: attribute.name + "_ftype",
			mode: 'local',
			store: store,
			valueField: 'id',
			displayField: 'type',
			triggerAction: 'all',
			forceSelection: true,
			allowBlank: true,
			width: 130
		});
		
	},
	/**
	 * The implementation must return an array to use as data of the store of the query combo
	 * The query combo is the combo-box in the attribute section of the Search window with the
	 * filtering options
	 */
	getQueryOptions: function() {
		throw new Error('not implemented');
	},	
	/**
	 * Template method, call the buildAttributeField method that must be implemented in the subclass
	 * @return Ext.form.Field or a subclass in order with the specific attribute
	 */
	buildField: function(attribute, hideLabel) {
		var field = this.buildAttributeField(attribute);
		field.hideLabel = hideLabel;
		return this.markAsRequired(field, attribute);		
	},	
	buildAttributeField: function() {
		throw new Error('not implemented');
	},	
	/**
	 * service function to add an asterisk before the label of a required attribute
	 */
	markAsRequired: function(field, attribute) {
		if (attribute.isnotnull || attribute.fieldmode == "required") {
			field.allowBlank = false;
			if (field.fieldLabel) {
				field.fieldLabel = '* ' + field.fieldLabel;
			}
		}
		return field;
	},	
	/**
	 * @return Ext.form.DisplayField
	 */
	buildReadOnlyField: function(attribute) {
		var field = new Ext.form.DisplayField ({
			labelAlign: "right",
			labelWidth: CMDBuild.CM_LABEL_WIDTH,
 			fieldLabel: attribute.description,
 			submitValue: false,
 			name: attribute.name,
 			disabled: false
		});
		return field;
	},
	/**
	 * The implementation must return a configuration object for the header of a Ext.GridPanel
	 */
	buildGridHeader: function(attribute) {
		throw new Error('not implemented');
	},	
	getDisplayNameForAttr: function(attribute) {
		return attribute.name;
	},
	/**
	 * @param attribute
	 * @return Ext.form.FieldSet 
	 * 
	 * this method prepare some variable and call the method buildFieldsetForFilter to have the fieldset
	 * in the subclass is possible override buildFieldsetForFilter to build a different fieldset 
	 */
	getFieldSetForFilter: function(attribute) {
		var fieldId = "_"+CMDBuild.Utils.nextId();
		var attributeCopy = Ext.apply({}, {
			fieldmode: "write", //change the fieldmode because in the filter must write on this field
			name: attribute.name+fieldId
		}, attribute);
		
		var field = this.buildField(attributeCopy, true);
		var query = this.getQueryCombo(attributeCopy); 
		var fieldset = this.buildFieldsetForFilter(fieldId,field,query,attributeCopy.description);
		return fieldset;
	},
	/**
	 * 
	 * @param fieldId
	 * @param field
	 * @param query
	 * @param originalFieldName
	 * 
	 * @return Ext.form.FieldSet
	 * 
	 * build a fieldSet with a combo-box and a field to edit a filtering criteria used in the
	 * attribute section of the filter.
	 */
	buildFieldsetForFilter: function(fieldId,field,query,originalFieldName) {
		var columnSeparetor = 20;
        
		var removeFieldButton = new Ext.button.Button({
			iconCls: 'delete',
            border: false
		});
		
		var orPanel = new Ext.Panel({
        	width: 30,
            html: 'or',
            border: false,
            bodyCls: "x-panel-body-default-framed"
        });

		function selectionNeedsNoValue(selection) {
			return ['null','notnull'].indexOf(selection[0].data.id) >= 0;
		}

		query.on('select',function(query, selection, id) {
			if (selectionNeedsNoValue(selection)) {
				field.disable();
			} else {
				field.enable();
			}
		});

		var fieldset = new Ext.panel.Panel({
            frame: false,
            border: false,
            bodyCls: "x-panel-body-default-framed",
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
        	items: [removeFieldButton,query,field,orPanel],
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
	}
};