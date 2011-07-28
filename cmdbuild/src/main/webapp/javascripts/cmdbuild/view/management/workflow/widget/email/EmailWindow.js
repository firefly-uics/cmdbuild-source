CMDBuild.Management.EmailWindow = Ext.extend(CMDBuild.PopupWindow, {
	emailGrid: undefined,
	readOnly: false,
	record: undefined,

	initComponent : function() {
		var body;
		if (this.readOnly) {
			body = {
				html: this.record.get("Content"),
				width: 500,
				height: 300,
				autoScroll: true
			};
		} else {
			body = {
				xtype : 'htmleditor',
				name : 'Content',
				fieldLabel : 'Content',
				hideLabel: true,
				enableLinks: false,
				enableSourceEdit: false,
				enableFont: false,
				value: this.record.get("Content")
			};
		}

		var formPanel = new Ext.form.FormPanel({
			autoWidth: true,
			autoHeight: true,
			frame: true,
			items : [{
					xtype: this.readOnly ? 'textfield' : 'hidden',
					name : 'FromAddress',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
					value: this.record.get("FromAddress"),
					width: 400,
					disabled: true
				},{
					xtype: 'textfield',
					vtype: this.readOnly ? undefined : 'emailaddrspec',
					name : 'ToAddresses',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
					value: this.record.get("ToAddresses"),
					width: 400,
					disabled: this.readOnly
				},{
					xtype: 'textfield',
					vtype: this.readOnly ? undefined : 'emailaddrspeclist',
					name : 'CcAddresses',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
					value: this.record.get("CcAddresses"),
					width: 400,
					disabled: this.readOnly
				},{
					xtype: 'textfield',
					name : 'Subject',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
					value: this.record.get("Subject"),
					width: 400,
					disabled: this.readOnly
				},body]
		});
    	this.form = formPanel.getForm();

    	var buttons;
    	if (this.readOnly) {
	    	buttons = [new CMDBuild.buttons.CloseButton({
				scope: this,
				handler: this.close
			})];
    	} else {
	    	buttons = [new CMDBuild.buttons.ConfirmButton({
				scope: this,
				handler: this.onConfirm
			}),new CMDBuild.buttons.AbortButton({
				scope: this,
				handler: this.close
			})];
    	}

		Ext.apply(this, {
			autoWidth: true,
			autoHeight: true,
			title: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,
			items : [formPanel],
			buttonAlign: 'center',
			buttons: buttons
		});

		CMDBuild.Management.EmailWindow.superclass.initComponent.apply(this, arguments);
	},

	onConfirm: function() {
		this.updateRecord();
		this.addOrUpdateGridRecord(this.record);
		this.close();
	},

	updateRecord: function() {
		var formValues = this.form.getValues();
		for (key in formValues) {
			this.record.set(key, formValues[key]);
		}
		this.record.set("Description", formValues["ToAddresses"]);
	},

	addOrUpdateGridRecord: function(updatedRecord) {
		this.emailGrid.addOrUpdateRecord(updatedRecord);
	}
});
