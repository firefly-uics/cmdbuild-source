Ext.define("CMDBuild.Management.EmailWindow", {
	extend: "CMDBuild.PopupWindow",

	emailGrid: undefined,
	readOnly: false,
	record: undefined,

	initComponent : function() {
		var body;
		if (this.readOnly) {
			body = {
				xtype: "panel",
				frame: true,
				border: true,
				html: this.record.get("Content"),
				autoScroll: true,
				flex: 1
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
				value: this.record.get("Content"),
				flex: 1
			};
		}

		var formPanel = new Ext.form.FormPanel({
			frame: false,
			border: false,
			padding: '5',
			bodyCls: "x-panel-body-default-framed",
			layout: {
				type: 'vbox',
				align: 'stretch' // Child items are stretched to full width
			},
			defaults: {
				labelAlign: "right"
			},
			items : [{
					xtype: this.readOnly ? 'displayfield' : 'hidden',
					name : 'FromAddress',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
					value: this.record.get("FromAddress")
				},{
					xtype: this.readOnly ? 'displayfield' : 'textfield',
					vtype: this.readOnly ? undefined : 'emailaddrspec',
					name : 'ToAddresses',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
					value: this.record.get("ToAddresses")
				},{
					xtype: this.readOnly ? 'displayfield' : 'textfield',
					vtype: this.readOnly ? undefined : 'emailaddrspeclist',
					name : 'CcAddresses',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
					value: this.record.get("CcAddresses")
				},{
					xtype: this.readOnly ? 'displayfield' : 'textfield',
					name : 'Subject',
					fieldLabel : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
					value: this.record.get("Subject")
				},body]
		});
		this.form = formPanel.getForm();

		var buttons,
			me = this;

		if (this.readOnly) {
			buttons = [
			new CMDBuild.buttons.CloseButton({
				handler: function() {
					me.close();
				}
			})];
		} else {
			buttons = [
				new CMDBuild.buttons.ConfirmButton({
					scope: this,
					handler: function() {
						me.updateRecord();
						me.emailGrid.addToStoreIfNotInIt(me.record);
						me.destroy();
					}
				}),
				new CMDBuild.buttons.AbortButton({
					handler: function() {
						me.close();
					}
				})
			];
		}

		Ext.apply(this, {
			title: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,
			items : [formPanel],
			buttonAlign: 'center',
			buttons: buttons
		});

		this.callParent(arguments);
	},

	updateRecord: function() {
		var formValues = this.form.getValues();
		for (var key in formValues) {
			this.record.set(key, formValues[key]);
		}
		this.record.set("Description", formValues["ToAddresses"]);
		this.record.commit();
	}
});
