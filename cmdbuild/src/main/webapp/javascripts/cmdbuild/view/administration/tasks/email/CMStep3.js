(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAlfrescoChecked':
					return this.onAlfrescoChecked();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Read CMDBuild's alfresco configuration from server and set Combobox store
		 */
		onAlfrescoChecked: function() {
			CMDBuild.ServiceProxy.configuration.read({
				scope: this,
				success: function(response) {
					var decodedJson = Ext.JSON.decode(response.responseText);

					this.view.alfrescoCombo.bindStore(
						CMDBuild.ServiceProxy.lookup.getLookupFieldStore(decodedJson.data['category.lookup'])
					);
				}
			}, name = 'dms');
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep3Delegate', this);

			// BodyParsing configuration
				this.bodyParsingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.bodyParsing,
					checkboxToggle: true,
					collapsed: true,
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'container',
							layout: 'hbox',

							defaults: {
								labelWidth: CMDBuild.LABEL_WIDTH,
								xtype: 'textfield'
							},

							items: [
								{
									fieldLabel: tr.keyInit,
									name: CMDBuild.ServiceProxy.parameter.KEY_INIT,
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								},
								{
									fieldLabel: tr.keyEnd,
									name: CMDBuild.ServiceProxy.parameter.KEY_END,
									margin: '0 0 0 20',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						},
						{
							xtype: 'container',
							layout: 'hbox',
							margin: '10 0',

							defaults: {
								labelWidth: CMDBuild.LABEL_WIDTH,
								xtype: 'textfield'
							},

							items: [
								{
									fieldLabel: tr.valueInit,
									name: CMDBuild.ServiceProxy.parameter.VALUE_INIT,
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								},
								{
									fieldLabel: tr.valueEnd,
									name: CMDBuild.ServiceProxy.parameter.VALUE_END,
									margin: '0 0 0 20',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						}
					]
				});
			// END: BodyParsing configuration

			// SendMail configuration
				this.emailTemplateCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					fieldLabel: tr.template,
					labelWidth: CMDBuild.LABEL_WIDTH,
					itemId: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					store: CMDBuild.core.proxy.CMProxyEmailTemplates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.sendMailFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.sendMail,
					checkboxToggle: true,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.emailTemplateCombo]
				});
			// END: SendMail configuration

			// Alfresco configuration
				this.alfrescoCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.ALFRESCO_LOOKUP_TYPE,
					fieldLabel: tr.alfrescoLookupType,
					labelWidth: CMDBuild.LABEL_WIDTH,
					itemId: CMDBuild.ServiceProxy.parameter.ALFRESCO_LOOKUP_TYPE,
					displayField: 'Description',
					valueField: 'Id',
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.alfrescoFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.saveToAlfresco,
					checkboxToggle: true,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.alfrescoCombo],
					listeners: {
						expand: function(fieldset, eOpts) {
							me.delegate.cmOn('onAlfrescoChecked');
						}
					}
				});
			// END: Alfresco configuration

			Ext.apply(this, {
				items: [
					this.bodyParsingFieldset,
					this.sendMailFieldset,
					this.alfrescoFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();
