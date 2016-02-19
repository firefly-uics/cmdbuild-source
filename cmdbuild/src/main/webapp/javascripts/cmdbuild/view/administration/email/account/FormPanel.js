(function() {

	Ext.define('CMDBuild.view.administration.email.account.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Account}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Delete}
		 */
		removeButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Check}
		 */
		setDefaultButton: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'x-panel-body-default-framed cmdb-border-top',
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'hbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyAccount,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountModifyButtonClick');
								}
							}),
							this.removeButton = Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeAccount,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountRemoveButtonClick');
								}
							}),
							this.setDefaultButton = Ext.create('CMDBuild.core.buttons.iconized.Check', {
								text: CMDBuild.Translation.setAsDefault,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountSetDefaultButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.container.Container', {
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.account,
								overflowY: 'auto',

								defaults: {
									labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
									maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
								},

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.NAME,
										fieldLabel: CMDBuild.Translation.name,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
										allowBlank: false,
										disableEnableFunctions: true
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.IS_DEFAULT,
										hidden: true
									}),
									Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
								]
							}),
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.credentials,
								overflowY: 'auto',

								defaults: {
									labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
									maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
								},

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									{
										xtype: 'textfield',
										name: CMDBuild.core.constants.Proxy.USERNAME,
										fieldLabel: CMDBuild.Translation.username
									},
									{
										xtype: 'textfield',
										inputType: 'password',
										name: CMDBuild.core.constants.Proxy.PASSWORD,
										fieldLabel: CMDBuild.Translation.password
									}
								]
							})
						]
					}),
					{ xtype: 'splitter' },
					Ext.create('Ext.container.Container', {
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.outgoing,
								overflowY: 'auto',

								defaults: {
									labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
									maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
								},

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									{
										xtype: 'textfield',
										name: CMDBuild.core.constants.Proxy.ADDRESS,
										fieldLabel: CMDBuild.Translation.address,
										allowBlank: false,
										vtype: 'email'
									},
									{
										xtype: 'textfield',
										name: CMDBuild.core.constants.Proxy.SMTP_SERVER,
										fieldLabel: CMDBuild.Translation.smtpServer
									},
									{
										xtype: 'numberfield',
										name: CMDBuild.core.constants.Proxy.SMTP_PORT,
										fieldLabel: CMDBuild.Translation.smtpPort,
										allowBlank: true,
										width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
										minValue: 1,
										maxValue: 65535,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL
									},
									{
										xtype: 'checkbox',
										name: CMDBuild.core.constants.Proxy.SMTP_SSL,
										fieldLabel: CMDBuild.Translation.enableSsl
									},
									{
										xtype: 'textfield',
										name: CMDBuild.core.constants.Proxy.OUTPUT_FOLDER,
										fieldLabel: CMDBuild.Translation.sentFolder
									}
								]
							}),
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.incoming,
								overflowY: 'auto',

								defaults: {
									labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
									maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
								},

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									{
										xtype: 'textfield',
										fieldLabel: CMDBuild.Translation.imapServer,
										name: CMDBuild.core.constants.Proxy.IMAP_SERVER
									},
									{
										xtype: 'numberfield',
										name: CMDBuild.core.constants.Proxy.IMAP_PORT,
										fieldLabel: CMDBuild.Translation.imapPort,
										allowBlank: true,
										minValue: 1,
										maxValue: 65535,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL
									},
									{
										xtype: 'checkbox',
										name: CMDBuild.core.constants.Proxy.IMAP_SSL,
										fieldLabel: CMDBuild.Translation.enableSsl
									}
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();