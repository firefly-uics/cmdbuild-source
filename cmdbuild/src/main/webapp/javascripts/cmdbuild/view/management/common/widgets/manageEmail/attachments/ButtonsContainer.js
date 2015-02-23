(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.attachments.ButtonsContainer', {
		extend: 'Ext.container.Container',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		delegate: undefined,

		addAttachmentFromDmsButton: undefined,
		attachmentFileField: undefined,
		attachmentUploadForm: undefined,
		attachmentUploadForm: undefined,

		layout: {
			type: 'hbox',
			padding: '0 5'
		},

		initComponent: function() {
			this.addAttachmentFromDmsButton = Ext.create('Ext.button.Button', {
				margin: '0 0 0 5',
				text: CMDBuild.Translation.add_attachment_from_dms,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onAddAttachmentFromDmsButtonClick');
				}
			});

			this.attachmentFileField = Ext.create('Ext.form.field.File', {
				name: 'file',
				buttonText: CMDBuild.Translation.attachfile,
				buttonOnly: true,
				scope: this,

				listeners: {
					scope: this,
					change: function(field, value, eOpts) {
						this.delegate.cmOn('onChangeAttachmentFile');
					}
				}
			});

			this.attachmentUploadForm = Ext.create('Ext.form.Panel', {
				frame: false,
				border: false,
				encoding: 'multipart/form-data',
				fileUpload: true,
				monitorValid: true,
				bodyCls: 'x-panel-body-default-framed',

				items: [this.attachmentFileField]
			});

			Ext.apply(this, {
				items: [this.attachmentUploadForm, this.addAttachmentFromDmsButton],
			});

			this.callParent(arguments);
		}
	});

})();