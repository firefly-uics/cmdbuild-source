(function() {

var check_suffix = "_check";

CMDBuild.Management.BulkCardAttributesPanel = Ext.extend(Ext.Panel, {
	translation : CMDBuild.Translation.management.modcard,
	autoScroll: true,
	initComponent : function() {
		this.actualForm = new Ext.form.FormPanel({
			autoScroll: true,
			monitorValid: true,
			labelAlign: "right", 
			labelWidth: 300,
			style: "padding: 10px",
			scope: this,
			items: [{
				xtype: 'hidden',
				name: 'IdClass'
			},{
				xtype: 'hidden',
				name: 'Id'
			}]
		});
		
		Ext.apply(this, {
			items: this.actualForm
        });
        
        this.formFields = [];
		CMDBuild.Management.BulkCardAttributesPanel.superclass.initComponent.apply(this, arguments);
	},
	
	resetForm: function() {
		this.actualForm.getForm().reset();
	},
	
	initForClass: function(eventParams) {
		if (this.currentClassId != eventParams.classId) {
			this.currentClassId = eventParams.classId;
			this.attributeList = eventParams.classAttributes;
			this.removeOldFields();			
			this.formFields = [];
			this.fillForm(eventParams);
		// Redraw the component
		this.doLayout();
		}
	},
	

	removeOldFields: function() {
		for (var i=0; i<this.formFields.length; i++) {
			var field = this.formFields[i];
			this.actualForm.remove(field);
		}
		this.doLayout();
	},
	
	fillForm: function(eventParams) {
		for (var i=0; i<this.attributeList.length; i++) {
			var attribute = this.attributeList[i];
			if (attribute.name != "Notes") {
				var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
				if (field) {
					var check = new Ext.ux.form.XCheckbox({
						// for the combo send the hiddenName
						name: (field.hiddenName || field.name) + check_suffix,
						field: field,
						labelSeparator: '',
						style: {padding: '0', margin: '0'},
						handler: function(box, checked){
							this.field.setDisabled(!checked);
						}
					})
					var fieldSet = new Ext.form.FieldSet({
						layout: 'column',
						field: field,
						labelWidth: 200,
						style: {padding: '0', 'border-bottom':'1px #CCC solid'},
						autoHeight: true,
						border: false,
						items: [{
							xtype: 'panel',
							width: 20,
							items: [check]
						}, {
							xtype: 'panel',
							layout: 'form',
							labelWidth: 200,
							// min-width fixes a ff rendering bug
							style: {padding: '0', margin: '0', 'min-width': '300px' },
							items: [field]
						}]
					})
					field.disable();
					this.formFields[i] = fieldSet;
					this.actualForm.add(fieldSet);
				}
			}
		}
		this.doLayout();
	},

	getCheckedValues: function() {
		var a = {};
		var formValues = this.actualForm.getForm().getValues();
		for (var i in formValues) {
			if (i.lastIndexOf(check_suffix) < 0) {
				var fieldEnabled = CMDBuild.Utils.evalBoolean(formValues[i + check_suffix]);
				if (fieldEnabled) {
					a[i] = formValues[i];
				}
			}
		}
		return a;
	}
});

})();