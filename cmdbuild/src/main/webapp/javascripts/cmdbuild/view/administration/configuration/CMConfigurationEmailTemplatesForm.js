(function() {

	var tr = CMDBuild.Translation.administration.setup.email.templates; // Path to translation

	Ext.define('CMDBuild.view.administration.configuration.CMConfigurationEmailTemplatesForm', {
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
						me.delegate.cmOn('onModifyButtonClick');
					}
				}),
				Ext.create('Ext.button.Button', {
					iconCls: 'delete',
					text: tr.remove,
					handler: function() {
						me.delegate.cmOn('onRemoveButtonClick');
					}
				})
			];

			this.cmButtons = [
				Ext.create('CMDBuild.buttons.SaveButton', {
					handler: function() {
						me.delegate.cmOn('onSaveButtonClick');
					}
				}),
				Ext.create('CMDBuild.buttons.AbortButton', {
					handler: function() {
						me.delegate.cmOn('onAbortButtonClick');
					}
				})
			];
			// END: Buttons configuration

			// Splitted-view wrapper
			this.nameField =Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.NAME,
				itemId: CMDBuild.ServiceProxy.parameter.NAME,
				fieldLabel: CMDBuild.Translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

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
						margins: '0px 3px 0px 0px',

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
							this.nameField,
							{
								xtype: 'textareafield',
								name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
								fieldLabel: CMDBuild.Translation.description_
							},
							{
								xtype: 'hiddenfield',
								name: CMDBuild.ServiceProxy.parameter.ID
							}
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.setup.email.templates.template,
						margins: '0px 0px 0px 3px',
						autoScroll: true,

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
							{
								name: CMDBuild.ServiceProxy.parameter.TO,
								fieldLabel: CMDBuild.Translation.to,
								allowBlank: false
							},
							{
								name: CMDBuild.ServiceProxy.parameter.CC,
								fieldLabel: CMDBuild.Translation.cc
							},
							{
								name: CMDBuild.ServiceProxy.parameter.BCC,
								fieldLabel: CMDBuild.Translation.bcc
							},
							{
								name: CMDBuild.ServiceProxy.parameter.SUBJECT,
								fieldLabel: CMDBuild.Translation.subject,
								allowBlank: false
							},
							Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
								name: CMDBuild.ServiceProxy.parameter.BODY,
								fieldLabel: CMDBuild.Translation.administration.setup.email.templates.body,
								labelWidth: CMDBuild.LABEL_WIDTH,
								considerAsFieldToDisable: true,
								enableFont: false
							})
						]
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
			this.nameField.setDisabled(true);
		}
	});

})();
