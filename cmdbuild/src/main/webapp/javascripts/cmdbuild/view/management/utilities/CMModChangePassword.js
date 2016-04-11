(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.core.proxy.utility.ChangePassword'
	]);

	Ext.define("CMDBuild.view.management.utilities.CMModChangePassword", {
		extend: "Ext.panel.Panel",
		cmName: 'changepassword',
		title : CMDBuild.Translation.management.modutilities.changepassword.title,

		constructor: function() {
			this.saveBtn = new Ext.button.Button({
				id:'confirmButton',
				text: CMDBuild.Translation.save,
				scope: this,
				handler: onSaveButtonClick
			});

			this.abortBtn = new Ext.button.Button({
				text: CMDBuild.Translation.cancel,
				scope: this,
				handler: function(){
					this.form.getForm().reset();
				}
			});

			this.form = new Ext.form.Panel({
				bodyCls: 'cmdb-blue-panel-no-padding',
				border: false,
				frame: false,
				region: "center",

				items: [{
					xtype: 'textfield',
					inputType:'password',
					fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.oldpw,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					name: 'oldpassword',
					allowBlank: false
				},{
					xtype: 'textfield',
					inputType:'password',
					fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.newpw,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					name: 'newpassword',
					id:'newpassword',
					allowBlank: false
				},{
					xtype: 'textfield',
					inputType:'password',
					fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.repnew,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					name: 'confirmnewpassword',
					vtype: 'password',
					twinFieldId: 'newpassword',
					allowBlank: false
				}]
			});

			Ext.apply(this, {
				frame: true,
				border: false,
				layout: "border",
				items:[this.form],
				buttonAlign: 'center',
				buttons: [this.saveBtn, this.abortBtn]
			});

			this.callParent(arguments);
		}
	});

	function onSaveButtonClick(){
		if (this.form.getForm().isValid()) {
			var params = this.form.getValues();

			CMDBuild.core.proxy.utility.ChangePassword.change({
				params: params,
				scope: this,
				callback: function (options, success, response) {
					this.form.getForm().reset()
				},
				success: function (response, options, decodedResponse) {
					CMDBuild.core.Message.info(null, CMDBuild.Translation.management.modutilities.changepassword.successmsgtext);
				}
			});
		} else {
			CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
		}
	}

})();