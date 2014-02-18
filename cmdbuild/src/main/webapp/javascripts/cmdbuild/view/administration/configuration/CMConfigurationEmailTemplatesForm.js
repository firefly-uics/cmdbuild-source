(function() {

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
					text: '@@ modify template',
					handler: function() {
						me.delegate.cmOn('onModifyButtonClick', me);
					}
				}),
				Ext.create('Ext.button.Button', {
					iconCls: 'delete',
					text: '@@ remove template',
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
			this.bccField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.BCC,
				fieldLabel: CMDBuild.Translation.bcc,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.bodyField = Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
				name: CMDBuild.ServiceProxy.parameter.BODY,
				fieldLabel: CMDBuild.Translation.administration.setup.email.templates.body,
				labelWidth: CMDBuild.LABEL_WIDTH,
				considerAsFieldToDisable: true,
				enableFont: false
			});

			this.ccField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.CC,
				fieldLabel: CMDBuild.Translation.cc,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.descriptionField = Ext.create('Ext.form.field.TextArea', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.nameField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE_NAME,
				fieldLabel: CMDBuild.Translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

			this.subjectField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.SUBJECT,
				fieldLabel: CMDBuild.Translation.subject,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

			this.toField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.TO,
				fieldLabel: CMDBuild.Translation.to,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.container.Container', {
				region: 'center',
				layout: {
					type: 'hbox',
					align: 'stretch'
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
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.modClass.attributeProperties.baseProperties,
						margins: '0px 3px 0px 0px',
						autoScroll: true,

						items: [this.nameField, this.descriptionField]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.administration.setup.email.templates.template,
						margins: '0px 0px 0px 3px',
						autoScroll: true,

						items: [this.toField, this.ccField, this.bccField, this.subjectField, this.bodyField]
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
		}
	});

})();
