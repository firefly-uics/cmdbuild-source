(function() {
	
Ext.ns("CMDBuild.administration.domain");
var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	
CMDBuild.administration.domain.CMDomainAttributeForm = Ext.extend(
		CMDBuild.administration.form.CMFormTemplate, {
	translation: CMDBuild.Translation.administration.modClass.attributeProperties,
	MODEL_TYPE: CMDBuild.core.model.CMAttributeModel,
	
	initComponent:function() {
		this.modifyButtonLabel = this.translation.modify_attribute;
		this.deleteButtonLabel = this.translation.delete_attribute;

		this.attributeName = new Ext.form.TextField({
			fieldLabel : this.translation.name,
			_name : this.MODEL_TYPE.STRUCTURE.name.name,
			name: "name",
			width : 220,
			disabled : true,
			allowBlank : false,
			vtype: "alphanum",
			CMDBuildReadonly: true
		});

		this.attributeDescription = new Ext.form.TextField({
			fieldLabel : this.translation.description,
			width : 220,
			_name : this.MODEL_TYPE.STRUCTURE.description.name,
			name: "description",
			disabled : true,
			allowBlank : false,
			vtype: "cmdbcomment"
		});

		this.attributeIsBaseDsp = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isbasedsp,
			_name: this.MODEL_TYPE.STRUCTURE.shownAsGridColumn.name,
			name: "isbasedsp",
			disabled: true
		});

		this.attributeNotNull = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isnotnull,
			_name: this.MODEL_TYPE.STRUCTURE.notnull.name,
			name: "isnotnull",
			disabled: true
		});

		this.attributeUnique = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isunique,
			_name: this.MODEL_TYPE.STRUCTURE.unique.name,
			name: "isunique",
			disabled: true
		});

		this.attributeActive = new Ext.ux.form.XCheckbox({
			fieldLabel: this.translation.isactive,
			_name: this.MODEL_TYPE.STRUCTURE.active.name,
			name: "isactive",
			disabled: true
		});

		this.attributeMode = new Ext.form.ComboBox({
			_name: this.MODEL_TYPE.STRUCTURE.editingMode.name,
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
			_name: this.MODEL_TYPE.STRUCTURE.type.name,
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
				autoLoad: true,
				url: "services/json/schema/modclass/getattributetypes",
				baseParams: {
					tableType: "DOMAIN"
				},
				root: "types",
				sortInfo: {
					field: "value",
					direction: "ASC"
				},
				fields: ["name", "value"]
			})
		});
		this.attributeType.setValue = this.attributeType.setValue.createSequence(onSelectComboType, this);

		this.stringLength = new  Ext.form.NumberField({
			fieldLabel : this.translation.length,
			minValue: 1,
			maxValue: Math.pow(2,31)-1,
			_name: this.MODEL_TYPE.STRUCTURE.stringLength.name,
			name : "len",
			allowBlank : false,
			disabled : true
		});

		this.decimalPrecision = new Ext.form.NumberField({
			fieldLabel : this.translation.precision,
			minValue: 1,
			maxValue: 20,
			_name: this.MODEL_TYPE.STRUCTURE.decimalPrecision.name,
			name : "precision",
			allowBlank : false,
			disabled : true
		});

		this.decimalScale = new Ext.form.NumberField({
			fieldLabel : this.translation.scale,
			minValue: 1,
			maxValue: 20,
			_name: this.MODEL_TYPE.STRUCTURE.decimalScale.name,
			name : "scale",
			allowBlank : false,
			disabled : true
		});

		this.lookupTypes = new Ext.form.ComboBox( {
			plugins: new CMDBuild.SetValueOnLoadPlugin(),
			fieldLabel: this.translation.lookup,
			_name: this.MODEL_TYPE.STRUCTURE.lookupType.name,
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
			title: translation.baseProperties,
			autoHeight: true,
			region: "center",
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
			title: translation.typeProperties,
			autoHeight: true,
			margins: "0 0 0 5",
			region: "east",
			width: "50%",
			items: [
				this.attributeType,
				this.decimalPrecision,
				this.decimalScale,
				this.stringLength,
				this.lookupTypes
			]
		});
		
		this.formPanelLayout = "border";
		this.formFields = [this.commonFields, this.typeSpecificFields];

		CMDBuild.administration.domain.CMDomainAttributeForm.superclass.initComponent.apply(this, arguments);

		this.typeSpecificFields.on({
			afterlayout:{
				scope: this,
				single: true,
				fn: hideContextualFields
			}
		});

		this.attributeName.on("change", function(fieldname, newValue, oldValue) {
			this.formPanel.autoComplete(this.attributeDescription, newValue, oldValue);
		}, this);
	},

	// override
	setDefaultValues: function() {
		this.attributeActive.setValue(true);
		this.attributeMode.setValue("write");
	},

	// override
	loadRecord: function(rec) {
		// this override can be removed when the remove the _name attribute
		// from the fields of the form
		var values = rec.data;
		this.cascade(function(item) {
			if (item && (item instanceof Ext.form.Field)) {
				var val = values[item._name] || "";
				item.setValue(val);
			}
		});
	},
	
	// override
	disableModify: function() {
		CMDBuild.administration.domain.CMDomainAttributeForm.superclass.disableModify.call(this);
		hideContextualFields.call(this);
	},
	
	// override
	enableModify: function(all) {
		CMDBuild.administration.domain.CMDomainAttributeForm.superclass.enableModify.call(this, all);
		disableContextualFieldsIfHidden.call(this);
	},

	// override
	prepareToAdd: function() {
		CMDBuild.administration.domain.CMDomainAttributeForm.superclass.prepareToAdd.call(this);
		hideContextualFields.call(this);
	}
});

	function onSelectComboType(type) {
		hideContextualFields.call(this, disable=true);
		showContextualFieldsByType.call(this, type);
	}
	
	function iterateOverContextualFields(type, fn) {
		var typeFields = this.contextualFields[type];
		if (typeFields) {
			for (var i=0, len=typeFields.length; i<len; i++) {
				fn(typeFields[i]);
			}
		}
	}
	
	function hideContextualFields(disable) {
		for (var type in this.contextualFields) {
			iterateOverContextualFields.call(this, type, function(field) {
				if (field.hideContainer) {
					field.hideContainer();
				} else {
					field.hide();
				}
				if (disable) {
					field.disable();
				}
			});
		}
	}
	
	function disableContextualFieldsIfHidden() {
		for (var type in this.contextualFields) {
			iterateOverContextualFields.call(this, type, function(field) {
				field.setDisabled(!field.isVisible());
			});
		}
	}
	
	function showContextualFieldsByType (type) {
		var t = this;
		iterateOverContextualFields.call(this,type, function(field) {
			if (field.showContainer) {
				field.showContainer();
			} else {
				// is not a field and has not the showContainer method
				field.show();
			}
			var toDisable = t.editingStatus == t.EDITING_STATUS.DISABLED;
			field.setDisabled(toDisable);
		});
	}
})();