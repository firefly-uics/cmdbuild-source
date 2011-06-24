CMDBuild.Management.ReportParamWin = Ext.extend(Ext.Window, {
	
	translation : CMDBuild.Translation.management.modreport,

	initComponent: function() {
		// save button
		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			formBind : true,
			scope : this,
			handler : function() {
	            this.submitParameters();
	        },
			disabled : true
		});

		// cancel button
		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : function() { this.close(); },
			scope : this
		});
		
		// formPanel
		this.formFields = {};
		this.formPanel = new Ext.FormPanel({
			labelWidth: 150,			
			defaults: { 
				labelWidth: 150
			},
			timeout: _CMDefault_timeout_s * 1000,
			labelAlign: "right",
			margins: '0 10 0 10',
			frame: true,
			border: true,
			monitorValid: true,
			items: [{}],
			buttonAlign: 'center',
			autoScroll: true,
			autoHeight: true,
			monitorResize: true,
			buttons: [
				this.saveButton,
				this.cancelButton
			]
		});
			
		var addFieldsToFormPanel = function() {
			for (var i=0; i<this.attributeList.length; i++) {
				var attribute = this.attributeList[i];
				var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);
				if (field) {
					if(attribute.defaultvalue) {
						field.setValue(attribute.defaultvalue);
					}
					this.formFields[i] = field;
					field.ownerCt = this.formPanel;
					field.labelWidth = 150; //for the multilevel lookup
					this.formPanel.items.add(field);
				}
			}
		}
		
		
		// window
		Ext.apply(this, {						
			id: 'reportParamWin',
			title: this.translation.report_parameters,
			width: '50%',			
			autoScroll: true,
			autoHeight: true,
			modal: true,
			layout:'fit',
			border:false,
			items: this.formPanel
		});				
		
		CMDBuild.Management.ReportParamWin.superclass.initComponent.apply(this);
		this.on("render", addFieldsToFormPanel, this)
	},	
	
	submitParameters: function() {
		CMDBuild.LoadMask.get().show();
		this.hide();
		var form = this.formPanel.getForm();
		if (form.isValid()) {
			form.submit({
				method: 'POST',
				url: 'services/json/management/modreport/updatereportfactoryparams',
				scope: this,
				success: function(form, action) {
					var popup = window.open("services/json/management/modreport/printreportfactory", "_blank");
					if (!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
					this.close();
					CMDBuild.LoadMask.get().hide();
				},
				failure: function() {
					this.show();
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	}		
});