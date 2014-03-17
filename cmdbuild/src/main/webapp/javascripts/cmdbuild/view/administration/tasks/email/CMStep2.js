(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep2Delegate', {

		delegate: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAlfrescoChecked':
					return this.onAlfrescoChecked(param.checked);

				case 'onBodyParsingChecked':
					return this.showComponent('bodyParsingKeysValues', param.checked);

				case 'onEmailChecked':
					return this.showComponent('emailTemplate', param.checked);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		showComponent: function(fieldName, showing) {
			var component = this.view.query('#' + fieldName)[0];

			if (showing) {
				component.show();
			} else {
				component.hide();
			}
		},

		onAlfrescoChecked: function(checked) {
			// Read CMDBuild's alfresco configuration from server and set combobox store
			CMDBuild.ServiceProxy.configuration.read({
				scope: this,
				success: function(response) {
					var decodedJson = Ext.JSON.decode(response.responseText);
					this.view.getForm().findField('alfrescoLookupType').bindStore(
						CMDBuild.ServiceProxy.lookup.getLookupFieldStore(decodedJson.data['category.lookup'])
					);
				}
			}, name = 'dms');

			return this.showComponent('alfrescoLookupType', checked);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep2', {
		extend: 'Ext.panel.Panel',

		taskType: 'email',

		border: false,
		height: '100%',

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep2Delegate');
			this.delegate.view = this;

			this.items = [
				{
					xtype: 'checkbox',
					fieldLabel: tr.bodyParsing,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn('onBodyParsingChecked', { 'checked': newValue });
						}
					}
				},
				{
					xtype: 'container',
					layout: 'vbox',
					itemId: 'bodyParsingKeysValues',
					hidden: true,
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
									margin: '0px 0px 0px 20px',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						},
						{
							xtype: 'container',
							layout: 'hbox',
							margin: '10px 0px',

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
									margin: '0px 0px 0px 20px',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						}
					]
				},
				{
					xtype: 'checkbox',
					fieldLabel: tr.sendMail,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn('onEmailChecked', { 'checked': newValue });
						}
					}
				},
				{
					xtype: 'combo',
					fieldLabel: tr.template,
					itemId: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					store: CMDBuild.core.serviceProxy.CMProxyConfigurationEmailTemplates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					hidden: true,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				},
				{
					xtype: 'checkbox',
					fieldLabel: tr.saveToAlfresco,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn('onAlfrescoChecked', { 'checked': newValue });
						}
					}
				},
				{
					xtype: 'combo',
					fieldLabel: tr.alfrescoLookupType,
					itemId: CMDBuild.ServiceProxy.parameter.ALFRESCO_LOOKUP_TYPE,
					name: CMDBuild.ServiceProxy.parameter.ALFRESCO_LOOKUP_TYPE,
					displayField: 'Description',
					valueField: 'Id',
					hidden: true,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				}
			];

			this.callParent(arguments);
		}
	});

})();