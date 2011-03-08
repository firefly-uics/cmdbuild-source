CMDBuild.Management.EditAttachmentWindow = Ext.extend(Ext.Window, {
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
			handler : function() { this.close() },
			scope : this
		});

		this.formPanel = new Ext.FormPanel({
			frame: false,
			border: false,
			bodyCssClass: "cmdbuild_background_blue cmdbuild_body_padding",
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
				}],
			buttonAlign: 'center',
			buttons: [
				this.saveButton,
				this.cancelButton
			]});

		Ext.apply(this, {
			id: 'editAttachmentsWindow',
			title: this.translation.window_title,
			width: 440,
			height: 200,
			modal: true,
			layout:'fit',
			items: [this.formPanel]
		});
		CMDBuild.Management.EditAttachmentWindow.superclass.initComponent
				.apply(this);
	},
	
	editAttachment: function() {
		this.disable();
		var fp = this.formPanel;
		if(fp.getForm().isValid()){
            fp.getForm().submit({
                url: 'services/json/management/modcard/modifyattachment',
                waitTitle : CMDBuild.Translation.common.wait_title,
				waitMsg : CMDBuild.Translation.common.wait_msg,
                success: function() {
                	// Defer the call because Alfresco is not responsive
                	function deferredCall() {
                		this.fireEvent('saved');
	                    this.close();
                	};
                	deferredCall.defer(CMDBuild.Config.dms.delay, this);
                },
                failure: function () {
                	this.enable();
                },
                scope: this
            });
        }
	}
});
Ext.reg('editattachmentwindow', CMDBuild.Management.EditAttachmentWindow);
