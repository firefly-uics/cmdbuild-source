(function() {

	var tr = CMDBuild.Translation.administration.email.accounts;

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
					text: tr.remove,
					scope: this,

					handler: function(button, e) {
						this.delegate.cmfg('onEmailAccountsRemoveButtonClick');
					}
				});

				this.setDefaultButton = Ext.create('CMDBuild.core.buttons.Check', {
					text: tr.setDefault,
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
					title: tr.account,
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
					title: tr.credentials,
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
							fieldLabel: tr.username
						},
						{
							xtype: 'textfield',
							inputType: 'password',
							name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
							fieldLabel: tr.password
						}
					]
				});

				this.outgoingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.outgoing,
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
							fieldLabel: tr.smtpServer
						},
						{
							xtype: 'numberfield',
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_PORT,
							fieldLabel: tr.smtpPort,
							allowBlank: true,
							width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
							minValue: 1,
							maxValue: 65535,
							maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH
						},
						{
							xtype: 'checkbox',
							name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SSL,
							fieldLabel: tr.enableSsl
						}
					]
				});

				this.incomingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.incoming,
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
							fieldLabel: tr.imapServer,
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SERVER
						},
						{
							xtype: 'numberfield',
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_PORT,
							fieldLabel: tr.imapPort,
							allowBlank: true,
							minValue: 1,
							maxValue: 65535,
							maxWidth: CMDBuild.ADM_SMALL_FIELD_WIDTH
						},
						{
							xtype: 'checkbox',
							name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SSL,
							fieldLabel: tr.enableSsl
						},
						{ // Splitter line
							xtype: 'container',
							margin: '5 0 5 0',
							maxWidth: '100%',
							cls: 'x-panel-body-default-framed cmborderbottom'
						},
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.INCOMING_FOLDER,
							fieldLabel: tr.incomingFolder
						},
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.PROCESSED_FOLDER,
							fieldLabel: tr.processedFolder
						},
						{
							xtype: 'textfield',
							name: CMDBuild.core.proxy.CMProxyConstants.REJECTED_FOLDER,
							fieldLabel: tr.rejectedFolder
						},
						{
							xtype: 'checkbox',
							name: CMDBuild.core.proxy.CMProxyConstants.ENABLE_MOVE_REJECTED_NOT_MATCHING,
							fieldLabel: tr.enableMoveRejectedNotMatching
						}
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
								text: tr.modify,
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