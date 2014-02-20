(function() {

	// FAKE DATAS
	var templates = Ext.create('Ext.data.Store', {
		fields: ['abbr', 'name'],
		data : [
			{'abbr':'1', 'name':'template 1'},
			{'abbr':'2', 'name':'template 2'},
			{'abbr':'3', 'name':'template 3'}
		]
	});
	// END FAKE DATAS

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep2Delegate', {

		delegate: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAlfrescoChecked': {

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

					return showComponent(this.view, 'alfrescoLookupType', param.checked);
				}

				case 'onBodyParsingChecked':
					return showComponent(this.view, 'bodyParsingKeysValues', param.checked);

				case 'onEmailChecked':
					return showComponent(this.view, 'emailTemplate', param.checked);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep2', {
		extend: 'Ext.form.Panel',

		border: false,
		bodyCls: 'cmgraypanel',
		height: '100%',

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.items = [
				{
					xtype: 'checkbox',
					fieldLabel: '@@ Body parsing',
					name: 'bodyParsing',
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
					name: 'bodyParsingKeysValues',
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
									fieldLabel: '@@ Key init',
									name: 'keyInit',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								},
								{
									fieldLabel: '@@ Key end',
									name: 'keyEnd',
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
									fieldLabel: '@@ Value init',
									name: 'valueInit',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								},
								{
									fieldLabel: '@@ Value end',
									name: 'valueEnd',
									margin: '0px 0px 0px 20px',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						}
					]
				},
				{
					xtype: 'checkbox',
					fieldLabel: '@@ Send mail',
					name: 'sendMail',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn('onEmailChecked', { 'checked': newValue });
						}
					}
				},
				{
					xtype: 'combo',
					fieldLabel: '@@ Template',
					itemId: 'emailTemplate',
					name: 'emailTemplate',
					store: CMDBuild.ServiceProxy.configuration.email.templates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					hidden: true,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				},
				{
					xtype: 'checkbox',
					fieldLabel: '@@ Save attachments to Alfresco',
					name: 'saveToAlfresco',
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					listeners: {
						change: function(that, newValue, oldValue, eOpts) {
							me.delegate.cmOn('onAlfrescoChecked', { 'checked': newValue });
						}
					}
				},
				{
					xtype: 'combo',
					fieldLabel: '@@ Alfresco lookup type',
					itemId: 'alfrescoLookupType',
					name: 'alfrescoLookupType',
					displayField: 'Description',
					valueField: 'Id',
					hidden: true,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				}
			];

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep2Delegate');
			this.delegate.view = this;

			this.callParent(arguments);
		}
	});

	function showComponent(view, fieldName, showing) {
		var component = view.query('#' + fieldName)[0];

		if (showing) {
			component.show();
		} else {
			component.hide();
		}
	}

})();
