(function() {

	var tr = CMDBuild.Translation.administration.setup.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.configuration.CMConfigurationEmailAccountsForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		delegate: undefined,

		autoScroll: false,
		buttonAlign: 'center',
		layout: 'fit',
		split: true,
		frame: false,
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		bodyCls: 'cmgraypanel',

		initComponent: function() {
			var me = this;

			// Buttons configuration
			this.cmTBar = [
				Ext.create('Ext.button.Button', {
					iconCls: 'modify',
					text: tr.modify,
					handler: function() {
						me.delegate.cmOn('onModifyButtonClick', me);
					}
				}),
				Ext.create('Ext.button.Button', {
					iconCls: 'delete',
					text: tr.remove,
					handler: function() {
						me.delegate.cmOn('onRemoveButtonClick', me);
					}
				})
			];

			this.cmButtons = [
				Ext.create('CMDBuild.buttons.SaveButton', {
					handler: function() {
						me.delegate.cmOn('onSaveButtonClick', me);
					}
				}),
				Ext.create('CMDBuild.buttons.AbortButton', {
					handler: function() {
						me.delegate.cmOn('onAbortButtonClick', me);
					}
				})
			];
			// END: Buttons configuration

			// Page FieldSets configuration
			this.emailAccount = Ext.create('Ext.form.FieldSet', {
				title: tr.account,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					xtype: 'textfield'
				},

				items: [
					{
						fieldLabel: tr.name,
						allowBlank: false,
						name: 'name'
					},
					{
						xtype: 'checkbox',
						fieldLabel: tr.isDefault,
						name: 'isDefault'
					},
					{
						xtype: 'hidden',
						name: 'id'
					}
				]
			});

			this.credentials = Ext.create('Ext.form.FieldSet', {
				title: tr.credentials,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					xtype: 'textfield'
				},

				items: [
					{
						fieldLabel: tr.username,
						allowBlank: false,
						name: 'username'
					},
					{
						inputType : 'password',
						fieldLabel: tr.password,
						allowBlank: false,
						name: 'password'
					}
				]
			});

			this.outgoing = Ext.create('Ext.form.FieldSet', {
				title: tr.outgoing,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					xtype: 'textfield'
				},

				items: [
					{
						fieldLabel: tr.address,
						allowBlank: false,
						name: 'address'
					},
					{
						fieldLabel: tr.smtpServer,
						name: 'smtpServer'
					},
					{
						xtype: 'numberfield',
						fieldLabel: tr.smtpPort,
						allowBlank: false,
						minValue: 1,
						maxValue: 65535,
						name: 'smtpPort'
					},
					{
						xtype: 'checkbox',
						fieldLabel: tr.enableSsl,
						name: 'smtpSsl'
					}
				]
			});

			this.incoming = Ext.create('Ext.form.FieldSet', {
				title: tr.incoming,

				items: [
					{
						xtype: 'container',
						padding: '0px 0px 5px 0px',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
							{
								fieldLabel: tr.imapServer,
								name: 'imapServer'
							},
							{
								xtype: 'numberfield',
								fieldLabel: tr.imapPort,
								allowBlank: false,
								minValue: 1,
								maxValue: 65535,
								name: 'imapPort'
							},
							{
								xtype: 'checkbox',
								fieldLabel: tr.enableSsl,
								name: 'imapSsl'
							}
						],
						cls: 'cmborderbottom'
					},
					{
						xtype: 'container',
						padding: '5px 0px',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
							{
								fieldLabel: tr.incomingFolder,
								name: 'incomingFolder'
							},
							{
								fieldLabel: tr.processedFolder,
								name: 'processedFolder'
							},
							{
								fieldLabel: tr.rejectedFolder,
								name: 'rejectedFolder'
							},
							{
								xtype: 'checkbox',
								fieldLabel: tr.enableMoveRejectedNotMatching,
								name: 'enableMoveRejectedNotMatching'
							}
						]
					}
				]
			});
			// END: Page FieldSets configuration

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.container.Container', {
				region: 'center',
				layout: {
					type: 'hbox',
					align:'stretch'
				},
				frame: false,
				border: false,

				defaults: {
					flex: 1,
					layout: {
						type: 'vbox',
						align: 'stretch'
					}
				},

				items: [
					{
						xtype: 'container',
						margins: '0px 3px 0px 0px',
						autoScroll: true,
						flex: 1,
						items: [this.emailAccount, this.credentials]
					},
					{
						xtype: 'container',
						margins: '0px 0px 0px 3px',
						autoScroll: true,
						flex: 1,
						items: [this.outgoing, this.incoming]
					}
				]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.wrapper],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
			this.disableCMButtons();
		},

		/**
		 * Disable isDefault checkbox, if it's checked, to avoid edit actions
		 */
		disableDefaultCheckbox: function() {
			var isDefault = this.getForm().findField('isDefault');

			if (isDefault.getValue()) {
				isDefault.disable();
			}
		}
	});

})();