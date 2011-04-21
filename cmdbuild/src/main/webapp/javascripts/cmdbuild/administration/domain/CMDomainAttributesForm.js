(function() {
	
Ext.ns("CMDBuild.administration.form");

CMDBuild.administration.form.CMFormTemplate = Ext.extend(Ext.Panel, {
	translation: CMDBuild.Translation.administration.modClass.attributeProperties,
	constructor: function() {
		this.modifyButton = new Ext.Button({	
			iconCls: "modify",
			text: this.translation.modify_attribute,
			handler: this.onModifyAction,
			scope: this
		});

		this.deleteButton = new Ext.Button({
			iconCls: "delete",
			text: this.translation.delete_attribute,
			handler: this.onDeleteAction,
			scope: this
		});

		this.attributeName = new Ext.form.TextField({
			fieldLabel : this.translation.name,
			name : "name",
			width : 220,
			disabled : true,
			allowBlank : false,
			vtype: "alphanum",
			CMDBuildReadonly: true
		});

		this.attributeDescription = new Ext.form.TextField({
			fieldLabel : this.translation.description,
			width : 220,
			name : "description",
			disabled : true,
			allowBlank : false,
			vtype: "cmdbcomment"
		});

		this.attributeIsBaseDsp = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isbasedsp,
			name: "isbasedsp",
			disabled: true
		});

		this.attributeNotNull = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isnotnull,
			name: "isnotnull",
			disabled: true
		});

		this.attributeUnique = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isunique,
			name: "isunique",
			disabled: true
		});

		this.attributeActive = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isactive,
			name: "isactive",
			disabled: true
		});

		this.attributeMode = new Ext.form.ComboBox({ 
			name: "fieldmode",
			fieldLabel: this.translation.field_visibility,
			valueField: "value",
			displayField: "name",
			hiddenName: "fieldmode",
			mode: "local",
			triggerAction: "all",
			editable: false,
			allowBlank: false,
			disabled: true,
			store: new Ext.data.SimpleStore({
				fields: ["value","name"],
				data : [
					["write",this.translation.field_write],
					["read",this.translation.field_read],
					["hidden",this.translation.field_hidden]
				]
			})
		});

		this.attributeType = new Ext.form.ComboBox({
			fieldLabel: this.translation.type,
			name: "type_value",
			hiddenName: "type",
			width: 220,
			triggerAction: "all",
			valueField: "name",
			displayField: "value",
			minChars: 0,
			allowBlank: false,
			editable: false,
			CMDBuildReadonly: true,
			store: new Ext.data.JsonStore({
				autoLoad: false,
				url: "services/json/schema/modclass/getattributetypes",
				root: "types",
				sortInfo: {
					field: "value",
					direction: "ASC"
				},
				fields: ["name", "value"]
			})
		});

		this.stringLength = new  Ext.form.NumberField({
			fieldLabel : this.translation.length,
			minValue: 1,
			maxValue: Math.pow(2,31)-1,
			name : "len",
			allowBlank : false,
			disabled : true
		});

		this.decimalPrecision = new Ext.form.NumberField({
			fieldLabel : this.translation.precision,
			minValue: 1,
			maxValue: 20,
			name : "precision",
			allowBlank : false,
			disabled : true
		});

		this.decimalScale = new Ext.form.NumberField({
			fieldLabel : this.translation.scale,
			minValue: 1,
			maxValue: 20,
			name : "scale",
			allowBlank : false,
			disabled : true
		});

		this.lookupTypes = new Ext.form.ComboBox( {
			plugins: new CMDBuild.SetValueOnLoadPlugin(),
			fieldLabel: this.translation.lookup,
			name: "lookup",
			triggerAction: "all",
			valueField: "type",
			displayField: "type",
			allowBlank: false,
			CMDBuildReadonly: true,
			store: CMDBuild.Cache.getLookupTypeLeavesAsStore(),
			mode: "local",
			disabled: true
		});

		this.contextualFields = {
			STRING: [this.stringLength],
			DECIMAL: [this.decimalPrecision,this.decimalScale],
			LOOKUP: [this.lookupTypes]
		};

		this.commonFields = new Ext.form.FieldSet({
			items: [
				this.attributeName,
				this.attributeDescription,
				this.attributeIsBaseDsp,
				this.attributeUnique,
				this.attributeNotNull,
				this.attributeActive,
				this.attributeMode
			]
		});
		
		this.typeSpecificFields = new Ext.form.FieldSet({
			items: [
				this.attributeType,
				this.decimalPrecision,
				this.decimalScale,
				this.stringLength,
				this.lookupTypes
			]
		});
		
		this.formPanel = new Ext.form.FormPanel({
			plugins: [new CMDBuild.CallbackPlugin(), new CMDBuild.FormPlugin()],
			layout:'hbox',
			layoutConfig: {
				padding:'5',
				align:'middle'
			},
			items: [this.commonFields, this.typeSpecificFields]
		});
		
		CMDBuild.administration.form.AttributeFormTemplate.superclass.constructor.apply(this, arguments);

		
	},
	
	initComponent:function() {
		this.tbar = [this.modifyButton, this.deleteButton];
		this.items = [this.formPanel];
//		this.buttonAlign = "center";
//		this.buttons = [this.saveButton, this.abortButton];
		
		CMDBuild.administration.form.AttributeFormTemplate.superclass.initComponent.apply(this, arguments);
		
		this.attributeType.on("select", onSelectComboType, this);
		this.typeSpecificFields.on({
			afterlayout:{
				scope: this,
				single: true,
				fn: hideContextualFields
			}
		});
	}
});

	function onSelectComboType(combo, record, index) {
		var type = record.data.value;
		hideContextualFields.call(this);
		showAndEnableContextualFieldsByType.call(this, type);
	}
	
	function iterateOverContextualFields(type, fn) {
		var typeFields = this.contextualFields[type];
		if (typeFields) {
			for (var i=0, len=typeFields.length; i<len; i++) {
				fn(typeFields[i]);
			}
		}
	}
	
	function hideContextualFields() {
		for (var type in this.contextualFields) {
			iterateOverContextualFields.call(this, type, function(field) {
				if (field.hideContainer) {
					field.hideContainer();
				} else {
					field.hide();
				}
				field.disable();
			});
		}
	}
	
	function showAndEnableContextualFieldsByType (type) {
		iterateOverContextualFields.call(this,type, function(field) {
			if (field.showContainer) {
				field.showContainer();
			} else {
				// is not a field and has not the showContainer method
				field.show();
			}
			field.enable();
		});
	}
})();