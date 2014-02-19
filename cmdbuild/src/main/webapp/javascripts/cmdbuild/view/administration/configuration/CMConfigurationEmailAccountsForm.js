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
				}),
				Ext.create('Ext.button.Button', {
					id: 'setDefaultButton',
					iconCls: 'ok',
					text: tr.setDefault,
					handler: function() {
						me.delegate.cmOn('onSetDefaultButtonClick', me);
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
						name: CMDBuild.ServiceProxy.parameter.NAME,
						id: CMDBuild.ServiceProxy.parameter.NAME,
						fieldLabel: CMDBuild.Translation.name,
						allowBlank: false
					},
					{
						xtype: 'hiddenfield',
						name: CMDBuild.ServiceProxy.parameter.IS_DEFAULT
					},
					{
						xtype: 'hiddenfield',
						name: CMDBuild.ServiceProxy.parameter.ID
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
						name: CMDBuild.ServiceProxy.parameter.USERNAME
					},
					{
						inputType : 'password',
						fieldLabel: tr.password,
						allowBlank: false,
						name: CMDBuild.ServiceProxy.parameter.PASSWORD
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
						name: CMDBuild.ServiceProxy.parameter.ADDRESS
					},
					{
						fieldLabel: tr.smtpServer,
						name: CMDBuild.ServiceProxy.parameter.SMTP_SERVER
					},
					{
						xtype: 'numberfield',
						fieldLabel: tr.smtpPort,
						allowBlank: false,
						minValue: 1,
						maxValue: 65535,
						name: CMDBuild.ServiceProxy.parameter.SMTP_PORT
					},
					{
						xtype: 'checkbox',
						fieldLabel: tr.enableSsl,
						name: CMDBuild.ServiceProxy.parameter.SMTP_SSL
					}
				]
			});

			this.incoming = Ext.create('Ext.form.FieldSet', {
				title: tr.incoming,

				items: [
					{
						xtype: 'container',
						padding: '0px 0px 5px 0px',
						cls: "x-panel-body-default-framed cmborderbottom",

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
								name: CMDBuild.ServiceProxy.parameter.IMAP_SERVER
							},
							{
								xtype: 'numberfield',
								fieldLabel: tr.imapPort,
								allowBlank: false,
								minValue: 1,
								maxValue: 65535,
								name: CMDBuild.ServiceProxy.parameter.IMAP_PORT
							},
							{
								xtype: 'checkbox',
								fieldLabel: tr.enableSsl,
								name: CMDBuild.ServiceProxy.parameter.IMAP_SSL
							}
						]
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
								name: CMDBuild.ServiceProxy.parameter.INCOMING_FOLDER
							},
							{
								fieldLabel: tr.processedFolder,
								name: CMDBuild.ServiceProxy.parameter.PROCESSED_FOLDER
							},
							{
								fieldLabel: tr.rejectedFolder,
								name: CMDBuild.ServiceProxy.parameter.REJECTED_FOLDER
							},
							{
								xtype: 'checkbox',
								fieldLabel: tr.enableMoveRejectedNotMatching,
								name: CMDBuild.ServiceProxy.parameter.ENABLE_MOVE_REJECTED_NOT_MATCHING
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
		 * Disable name field
		 */
		disableNameField: function() {
			Ext.getCmp(CMDBuild.ServiceProxy.parameter.NAME).setDisabled(true);
		},

		/**
		 * Disable setDefaultButton, if selected account is default
		 */
		disableSetDefaultButton: function() {
			Ext.getCmp('setDefaultButton').setDisabled(
				this.getForm().findField(CMDBuild.ServiceProxy.parameter.IS_DEFAULT).getValue() === 'true'
			);
		}
	});

})();