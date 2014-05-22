(function() {

	var tr = CMDBuild.Translation.administration.email.templates; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplatesForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		delegate: undefined,

		autoScroll: false,
		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'fit',
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

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.container.Container', {
				region: 'center',
				frame: false,
				border: false,

				layout: {
					type: 'hbox',
					align: 'stretch'
				},

				defaults: {
					flex: 1,
					layout: {
						type: 'vbox',
						align: 'stretch'
					}
				},

				items: [
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.modClass.attributeProperties.baseProperties,
						margins: '0 3 0 0',

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH
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
							}
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.email.templates.template,
						margins: '0 0 0 3',
						autoScroll: true,

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
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
								considerAsFieldToDisable: true,
								enableFont: false
							})
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