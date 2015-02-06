(function() {

	var tr = CMDBuild.Translation.administration.email.templates;

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplatesForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyEmailAccounts'
		],

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.email.CMEmailTemplatesController}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		addVariablesButton: undefined,

		/**
		 * @property {Array}
		 */
		cmButtons: undefined,

		/**
		 * @property {Array}
		 */
		cmTBar: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMErasableCombo}
		 */
		defaultAccountCombo: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		nameField: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'border',
		split: true,

		initComponent: function() {
			// Buttons configuration
				this.cmTBar = [
					Ext.create('Ext.button.Button', {
						iconCls: 'modify',
						text: tr.modify,
						scope: this,

						handler: function() {
							this.delegate.cmOn('onModifyButtonClick');
						}
					}),
					Ext.create('Ext.button.Button', {
						iconCls: 'delete',
						text: tr.remove,
						scope: this,

						handler: function() {
							this.delegate.cmOn('onRemoveButtonClick');
						}
					})
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

			this.nameField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.NAME,
				itemId: CMDBuild.core.proxy.CMProxyConstants.NAME,
				fieldLabel: CMDBuild.Translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

			this.addVariablesButton = Ext.create('Ext.button.Button', {
				text: tr.valuesWindow.title,
				scope: this,
				iconCls: 'modify',
				anchor: 'auto',
				margin: '0 0 0 ' + (CMDBuild.LABEL_WIDTH + 5),

				handler: function() {
					this.delegate.cmOn('onVariablesButtonClick');
				}
			});

			this.defaultAccountCombo = Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
				name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT,
				fieldLabel: CMDBuild.Translation.defaultAccount,
				labelWidth: CMDBuild.LABEL_WIDTH,
				displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false,

				store: CMDBuild.core.proxy.CMProxyEmailTemplates.getEmailAccountsStore(),
				queryMode: 'local'
			});

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
				region: 'center',
				frame: true,
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
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.modClass.attributeProperties.baseProperties,
						flex: 1,

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
							anchor: '100%'
						},

						items: [
							this.nameField,
							{
								xtype: 'textareafield',
								name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
								fieldLabel: CMDBuild.Translation.description_
							},
							{
								xtype: 'hiddenfield',
								name: CMDBuild.core.proxy.CMProxyConstants.ID
							},
							{
								xtype: 'checkbox',
								fieldLabel: '@@ Automatic synchronization',
								name: '@@ autoSync'
							},
							{
								xtype: 'checkbox',
								fieldLabel: '@@ Report desync',
								name: '@@ reportDesynch'
							}
						]
					},
					{ xtype: 'splitter' },
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.email.templates.template,
						flex: 1,

						defaults: {
							xtype: 'textfield',
							labelWidth: CMDBuild.LABEL_WIDTH,
							maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
							anchor: '100%'
						},

						items: [
							this.defaultAccountCombo,
							{
								name: CMDBuild.core.proxy.CMProxyConstants.TO,
								fieldLabel: CMDBuild.Translation.to,
								allowBlank: false
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.CC,
								fieldLabel: CMDBuild.Translation.cc
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.BCC,
								fieldLabel: CMDBuild.Translation.bcc
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
								fieldLabel: CMDBuild.Translation.subject,
								allowBlank: false
							},
							Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
								name: CMDBuild.core.proxy.CMProxyConstants.BODY,
								fieldLabel: CMDBuild.Translation.administration.email.templates.body,
								labelWidth: CMDBuild.LABEL_WIDTH,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								considerAsFieldToDisable: true
							}),
							this.addVariablesButton
						]
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
		}
	});

})();