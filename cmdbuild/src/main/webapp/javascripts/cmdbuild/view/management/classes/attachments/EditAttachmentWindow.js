Ext.define("CMDBuild.Management.EditAttachmentWindow", {
	extend: "Ext.window.Window",
	translation: CMDBuild.Translation.management.modcard.add_attachment_window,

	initComponent: function() {
		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			formBind : true,
			handler : this.editAttachment,
			scope : this
		});

		this.cancelButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			name: 'cancelButton',
			handler : function() { this.close(); },
			scope : this
		});

		this.formPanel = new Ext.FormPanel({
			frame: true,
			border: false,
			labelAlign: 'right',
			monitorValid: true,
			items: [{
				name: 'IdClass',
				xtype: 'hidden',
				value: this.classId
			},{
				name: 'Id',
				xtype: 'hidden',
				value: this.cardId
			},{
				name: 'Filename',
				xtype: 'hidden',
				value: this.filename
			},{
				xtype: 'textfield',
				fieldLabel: this.translation.category,
				readOnly: true,
				value: this.category,
				disabled: true,
				width: 300
			},{
				xtype: 'textfield',
				fieldLabel: this.translation.load_attachment,
				readOnly: true,
				value: this.filename,
				disabled: true,
				width: 300
			},{
				name: 'Description',
				xtype: 'textarea',
				fieldLabel: this.translation.description,
				allowBlank: false,
				value: this.description,
				width: 300
			}]
		});

		Ext.apply(this, {
			title: this.translation.window_title,
			items: [this.formPanel],
			autoScroll: true,
			autoHeight: true,
			modal: true,
			layout:'fit',
			frame: false,
			border: false,
			buttonAlign: 'center',
			buttons: [
				this.saveButton,
				this.cancelButton
			]
		});

		this.callParent(arguments);
	},

	editAttachment: function() {
		var fp = this.formPanel;
		if(fp.getForm().isValid()) {
			CMDBuild.LoadMask.get().show();
			fp.getForm().submit({
				url: 'services/json/attachments/modifyattachment',
				scope: this,
				success: function() {
					// Defer the call because Alfresco is not responsive
					function deferredCall() {
						CMDBuild.LoadMask.get().hide();
						this.fireEvent('saved');
						this.close();
					};

					Ext.Function.createDelayed(deferredCall, CMDBuild.Config.dms.delay, this)();
				},
				failure: function () {
					this.enable();
				}
			});
		}
	}

});