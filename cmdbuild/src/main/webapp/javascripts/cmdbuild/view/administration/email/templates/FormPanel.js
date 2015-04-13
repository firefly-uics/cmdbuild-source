(function() {

	var tr = CMDBuild.Translation.administration.email.templates;

	Ext.define('CMDBuild.view.administration.email.templates.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyEmailAccounts'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Main}
		 */
		delegate: undefined,

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
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'fit',
		split: true,

		initComponent: function() {
			this.nameField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.NAME,
				itemId: CMDBuild.core.proxy.CMProxyConstants.NAME,
				fieldLabel: CMDBuild.Translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,
				cmImmutable: true
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

				store: CMDBuild.core.proxy.CMProxyEmailAccounts.getStore(),
				queryMode: 'local'
			});

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.panel.Panel', {
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
								fieldLabel: CMDBuild.Translation.descriptionLabel
							},
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.keepSync,
								inputValue: true,
								uncheckedValue: false,
								name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION
							},
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.promptSync,
								inputValue: true,
								uncheckedValue: false,
								name: CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION
							},
							{
								xtype: 'hiddenfield',
								name: CMDBuild.core.proxy.CMProxyConstants.ID
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
								name: CMDBuild.core.proxy.CMProxyConstants.FROM,
								fieldLabel: CMDBuild.Translation.from,
								vtype: 'email'
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.TO,
								fieldLabel: CMDBuild.Translation.to
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
								fieldLabel: CMDBuild.Translation.subject
							},
							Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
								name: CMDBuild.core.proxy.CMProxyConstants.BODY,
								fieldLabel: CMDBuild.Translation.administration.email.templates.body,
								labelWidth: CMDBuild.LABEL_WIDTH,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								considerAsFieldToDisable: true
							}),
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: tr.valuesWindow.title,
								anchor: 'auto',
								margin: '0 0 0 ' + (CMDBuild.LABEL_WIDTH + 5),
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplatesValuesButtonClick');
								}
							})
						]
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
									this.delegate.cmfg('onEmailTemplatesModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Delete', {
								text: tr.remove,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplatesRemoveButtonClick');
								}
							})
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
									this.delegate.cmfg('onEmailTemplatesSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplatesAbortButtonClick');
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