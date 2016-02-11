(function() {

	Ext.define('CMDBuild.view.administration.email.accounts.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.email.Accounts}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		credentialsFieldset: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		emailAccountFieldset: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		incomingFieldset: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		isDefaultField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		nameField: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		outgoingFieldset: undefined,

		/**
		 * @property {CMDBuild.core.buttons.Delete}
		 */
		removeButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.Check}
		 */
		setDefaultButton: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'fit',
		split: true,

		initComponent: function() {
			// Buttons configuration
				this.removeButton = Ext.create('CMDBuild.core.buttons.Delete', {
					text: CMDBuild.Translation.removeAccount,
					scope: this,

					handler: function(button, e) {
						this.delegate.cmfg('onEmailAccountsRemoveButtonClick');
					}
				});

				this.setDefaultButton = Ext.create('CMDBuild.core.buttons.Check', {
					text: CMDBuild.Translation.setAsDefault,
					scope: this,

					handler: function(button, e) {
						this.delegate.cmfg('onEmailAccountsSetDefaultButtonClick');
					}
				});
			// END: Buttons configuration

			// Page FieldSets configuration
				// Account
				this.nameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.proxy.CMProxyConstants.NAME,
					itemId: CMDBuild.core.proxy.CMProxyConstants.NAME,
					fieldLabel: CMDBuild.Translation.name,
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					allowBlank: false,
					cmImmutable: true
				});

				this.isDefaultField = Ext.create('Ext.form.field.Checkbox', {
					name: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT,
					hidden: true
				});

				this.emailAccountFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.account,
					overflowY: 'auto',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						anchor: '100%'
					},

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

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
				this.credentialsFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.credentials,
					overflowY: 'auto',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						anchor: '100%'
					},

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
							fieldLabel: CMDBuild.Translation.username
						},
						{
							xtype: 'textfield',
							inputType: 'password',
							name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
							fieldLabel: CMDBuild.Translation.password
						}
					]
				});

				// Outgoing
				this.outgoingFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.outgoing,
					overflowY: 'auto',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						anchor: '100%'
					},

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.ADDRESS,
							fieldLabel: CMDBuild.Translation.address,
							allowBlank: false,
							vtype: 'email'
						},
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SERVER,
							fieldLabel: CMDBuild.Translation.smtpServer
						},
						{
							xtype: 'numberfield',
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_PORT,
							fieldLabel: CMDBuild.Translation.smtpPort,
							allowBlank: true,
							width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
							minValue: 1,
							maxValue: 65535,
							maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH
						},
						{
							xtype: 'checkbox',
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SSL,
							fieldLabel: CMDBuild.Translation.enableSsl
						},
						Ext.create('Ext.form.field.Checkbox',{
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_START_TLS,
							fieldLabel: CMDBuild.Translation.enableStartTls,
							labelWidth: CMDBuild.LABEL_WIDTH,
							inputValue: true,
							uncheckedValue: false
						}),
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.OUTPUT_FOLDER,
							fieldLabel: CMDBuild.Translation.sentFolder
						}
					]
				});

				// Incoming
				this.incomingFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.incoming,
					overflowY: 'auto',

					defaults: {
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						anchor: '100%'
					},

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						{
							xtype: 'textfield',
							fieldLabel: CMDBuild.Translation.imapServer,
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SERVER
						},
						{
							xtype: 'numberfield',
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_PORT,
							fieldLabel: CMDBuild.Translation.imapPort,
							allowBlank: true,
							minValue: 1,
							maxValue: 65535,
							maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH
						},
						{
							xtype: 'checkbox',
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SSL,
							fieldLabel: CMDBuild.Translation.enableSsl
						},
						Ext.create('Ext.form.field.Checkbox',{
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_START_TLS,
							fieldLabel: CMDBuild.Translation.enableStartTls,
							labelWidth: CMDBuild.LABEL_WIDTH,
							inputValue: true,
							uncheckedValue: false
						})
					]
				});
			// END: Page FieldSets configuration

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
				bodyCls: 'cmgraypanel-nopadding',
				frame: false,
				border: false,

				layout: {
					type: 'hbox',
					align: 'stretch'
				},

				defaults: {
					overflowX: 'auto',
					overflowY: 'auto'
				},

				items: [
					{
						xtype: 'container',
						flex: 1,
						items: [this.emailAccountFieldset, this.credentialsFieldset]
					},
					{ xtype: 'splitter' },
					{
						xtype: 'container',
						flex: 1,
						items: [this.outgoingFieldset, this.incomingFieldset]
					}
				]
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyAccount,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountsModifyButtonClick');
								}
							}),
							this.removeButton,
							this.setDefaultButton
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailAccountsAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.wrapper]
			});

			this.callParent(arguments);

			this.setDisabledModify(true);
		}
	});

})();