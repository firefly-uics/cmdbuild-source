(function() {

	Ext.define('CMDBuild.view.administration.email.templates.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.email.Account'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Templates}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMErasableCombo}
		 */
		defaultAccountCombo: undefined,

		/**
		 * @property {CMDBuild.view.common.field.delay.Delay}
		 */
		delayField: undefined,

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
				name: CMDBuild.core.constants.Proxy.NAME,
				itemId: CMDBuild.core.constants.Proxy.NAME,
				fieldLabel: CMDBuild.Translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,
				cmImmutable: true
			});

			this.defaultAccountCombo = Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
				name: CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT,
				fieldLabel: CMDBuild.Translation.defaultAccount,
				labelWidth: CMDBuild.LABEL_WIDTH,
				displayField: CMDBuild.core.constants.Proxy.NAME,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false,

				store: CMDBuild.core.proxy.email.Account.getStore(true),
				queryMode: 'local'
			});

			this.delayField = Ext.create('CMDBuild.view.common.field.delay.Delay', {
				fieldLabel: CMDBuild.Translation.delay,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.core.constants.Proxy.DELAY
			});

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.form.Panel', {
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
						xtype: 'fieldset',
						title: CMDBuild.Translation.baseProperties,
						flex: 1,

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
							{
								xtype: 'textareafield',
								name: CMDBuild.core.constants.Proxy.DESCRIPTION,
								fieldLabel: CMDBuild.Translation.descriptionLabel
							},
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.keepSync,
								inputValue: true,
								uncheckedValue: false,
								name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION
							},
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.promptSync,
								inputValue: true,
								uncheckedValue: false,
								name: CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION
							},
							this.delayField,
							{
								xtype: 'hiddenfield',
								name: CMDBuild.core.constants.Proxy.ID
							}
						]
					},
					{ xtype: 'splitter' },
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.template,
						flex: 1,

						defaults: {
							xtype: 'textfield',
							labelWidth: CMDBuild.LABEL_WIDTH,
							maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
							anchor: '100%'
						},

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							this.defaultAccountCombo,
							{
								name: CMDBuild.core.constants.Proxy.FROM,
								fieldLabel: CMDBuild.Translation.from,
								vtype: 'email'
							},
							{
								name: CMDBuild.core.constants.Proxy.TO,
								fieldLabel: CMDBuild.Translation.to
							},
							{
								name: CMDBuild.core.constants.Proxy.CC,
								fieldLabel: CMDBuild.Translation.cc
							},
							{
								name: CMDBuild.core.constants.Proxy.BCC,
								fieldLabel: CMDBuild.Translation.bcc
							},
							{
								name: CMDBuild.core.constants.Proxy.SUBJECT,
								fieldLabel: CMDBuild.Translation.subject
							},
							Ext.create('CMDBuild.view.common.field.HtmlEditor', {
								name: CMDBuild.core.constants.Proxy.BODY,
								fieldLabel: CMDBuild.Translation.body,
								labelWidth: CMDBuild.LABEL_WIDTH,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.editValues,
								margin: '0 0 0 ' + (CMDBuild.LABEL_WIDTH + 5),
								maxWidth: 100,
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
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyTemplate,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplatesModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Delete', {
								text: CMDBuild.Translation.removeTemplate,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplatesRemoveButtonClick');
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
									this.delegate.cmfg('onEmailTemplatesSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
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

			this.setDisabledModify(true, true, true);
		}
	});

})();