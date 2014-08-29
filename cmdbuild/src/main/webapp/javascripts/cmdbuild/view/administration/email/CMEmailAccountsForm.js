(function() {

	var tr = CMDBuild.Translation.administration.email.accounts;

	Ext.define('CMDBuild.view.administration.email.CMEmailAccountsForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'border',
		split: true,

		initComponent: function() {
			// Buttons configuration
			this.removeButton = Ext.create('Ext.button.Button', {
				id: 'removeButton',
				iconCls: 'delete',
				text: tr.remove,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onRemoveButtonClick');
				}
			});

			this.setDefaultButton = Ext.create('Ext.button.Button', {
				id: 'setDefaultButton',
				iconCls: 'ok',
				text: tr.setDefault,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onSetDefaultButtonClick');
				}
			});

			this.cmTBar = [
				Ext.create('Ext.button.Button', {
					iconCls: 'modify',
					text: tr.modify,
					scope: this,

					handler: function() {
						this.delegate.cmOn('onModifyButtonClick');
					}
				}),
				this.removeButton,
				this.setDefaultButton
			];

			this.cmButtons = [
				Ext.create('CMDBuild.buttons.SaveButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onSaveButtonClick');
					}
				}),
				Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,

					handler: function() {
						this.delegate.cmOn('onAbortButtonClick');
					}
				})
			];
			// END: Buttons configuration

			// Page FieldSets configuration
				// Account
				this.nameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.proxy.CMProxyConstants.NAME,
					itemId: CMDBuild.core.proxy.CMProxyConstants.NAME,
					fieldLabel: CMDBuild.Translation.name,
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					anchor: '100%',
					allowBlank: false
				});

				this.isDefaultField = Ext.create('Ext.form.field.Checkbox', {
					hidden: true,
					name: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT
				});

				this.emailAccount = Ext.create('Ext.form.FieldSet', {
					title: tr.account,
					overflowY: 'auto',

					items: [
						this.nameField,
						this.isDefaultField,
						{
							xtype: 'hiddenfield',
							name: CMDBuild.core.proxy.CMProxyConstants.ID
						}
					]
				});

				// Credentials
				this.credentials = Ext.create('Ext.form.FieldSet', {
					title: tr.credentials,
					overflowY: 'auto',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						xtype: 'textfield',
						anchor: '100%'
					},

					items: [
						{
							fieldLabel: tr.username,
							allowBlank: false,
							name: CMDBuild.core.proxy.CMProxyConstants.USERNAME
						},
						{
							inputType: 'password',
							fieldLabel: tr.password,
							allowBlank: false,
							name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD
						}
					]
				});

				this.outgoing = Ext.create('Ext.form.FieldSet', {
					title: tr.outgoing,
					overflowY: 'auto',

					defaults: {
						xtype: 'textfield',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						anchor: '100%'
					},

					items: [
						{
							fieldLabel: CMDBuild.Translation.address,
							allowBlank: false,
							vtype: 'email',
							name: CMDBuild.core.proxy.CMProxyConstants.ADDRESS
						},
						{
							fieldLabel: tr.smtpServer,
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SERVER
						},
						{
							xtype: 'numberfield',
							fieldLabel: tr.smtpPort,
							allowBlank: true,
							width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
							minValue: 1,
							maxValue: 65535,
							maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH,
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_PORT
						},
						{
							xtype: 'checkbox',
							fieldLabel: tr.enableSsl,
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SSL
						}
					]
				});

				this.incoming = Ext.create('Ext.form.FieldSet', {
					title: tr.incoming,
					overflowY: 'auto',

					defaults: {
						xtype: 'textfield',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						anchor: '100%'
					},

					items: [
						{
							fieldLabel: tr.imapServer,
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SERVER
						},
						{
							xtype: 'numberfield',
							fieldLabel: tr.imapPort,
							allowBlank: true,
							minValue: 1,
							maxValue: 65535,
							maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH,
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_PORT
						},
						{
							xtype: 'checkbox',
							fieldLabel: tr.enableSsl,
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SSL
						},
						{ // Splitter line
							xtype: 'container',
							margin: '5 0 5 0',
							maxWidth: '100%',
							cls: 'x-panel-body-default-framed cmborderbottom'
						},
						{
							fieldLabel: tr.incomingFolder,
							name: CMDBuild.core.proxy.CMProxyConstants.INCOMING_FOLDER
						},
						{
							fieldLabel: tr.processedFolder,
							name: CMDBuild.core.proxy.CMProxyConstants.PROCESSED_FOLDER
						},
						{
							fieldLabel: tr.rejectedFolder,
							name: CMDBuild.core.proxy.CMProxyConstants.REJECTED_FOLDER
						},
						{
							xtype: 'checkbox',
							fieldLabel: tr.enableMoveRejectedNotMatching,
							name: CMDBuild.core.proxy.CMProxyConstants.ENABLE_MOVE_REJECTED_NOT_MATCHING
						}
					]
				});
			// END: Page FieldSets configuration

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
				region: 'center',
				frame: true,
				border: false,

				layout: {
					type: 'hbox',
					align:'stretch'
				},

				defaults: {
					overflowY: 'auto',
					flex: 1
				},

				items: [
					{
						xtype: 'container',
						margins: '0 3 0 0',
						items: [this.emailAccount, this.credentials]
					},
					{
						xtype: 'container',
						margins: '0 0 0 3',
						items: [this.outgoing, this.incoming]
					}
				]
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: this.cmTBar
					}
				],
				items: [this.wrapper],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
			this.disableCMButtons();
		},

		disableNameField: function() {
			this.nameField.setDisabled(true);
		},

		/**
		 * Disable setDefaultButton and removeButton, if selected account is default
		 */
		disableSetDefaultAndRemoveButton: function() {
			this.setDefaultButton.setDisabled(
				this.isDefaultField.getValue()
			);

			this.removeButton.setDisabled(
				this.isDefaultField.getValue()
			);
		}
	});

})();