(function() {

	Ext.define('CMDBuild.view.administration.configuration.DmsPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Dms}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		fieldDefaults: {
			labelAlign: 'left',
			labelWidth: CMDBuild.CFG_LABEL_WIDTH,
			maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
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
									this.delegate.cmfg('onConfigurationAlfrescoSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationAlfrescoAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.general,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								xtype: 'checkbox',
								name: CMDBuild.core.constants.Proxy.ENABLED,
								fieldLabel: CMDBuild.Translation.enabled,
								inputValue: true,
								uncheckedValue: false
							},
							{
								name: CMDBuild.core.constants.Proxy.SERVER_URL,
								fieldLabel: CMDBuild.Translation.host,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.DELAY,
								fieldLabel: CMDBuild.Translation.operationsDelay,
								allowBlank: false
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.fileServer,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								name: CMDBuild.core.constants.Proxy.FILE_SERVER_TYPE,
								fieldLabel: CMDBuild.Translation.type,
								allowBlank: false,
								disabled: true
							},
							{
								name: CMDBuild.core.constants.Proxy.FILE_SERVER_URL,
								fieldLabel: CMDBuild.Translation.host,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: CMDBuild.core.constants.Proxy.FILE_SERVER_PORT,
								fieldLabel: CMDBuild.Translation.port,
								allowBlank: false
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.repository,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								name: CMDBuild.core.constants.Proxy.REPOSITORY_FILE_SERVER_PATH,
								fieldLabel: CMDBuild.Translation.fileServerPath,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								name: CMDBuild.core.constants.Proxy.REPOSITORY_WEB_SERVICE_PATH,
								fieldLabel: CMDBuild.Translation.webServicePath,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								name: CMDBuild.core.constants.Proxy.REPOSITORY_APPLICATION,
								fieldLabel: CMDBuild.Translation.application,
								allowBlank: false
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.credentials,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								name: CMDBuild.core.constants.Proxy.USER,
								fieldLabel: CMDBuild.Translation.username,
								allowBlank: false
							},
							{
								name: CMDBuild.core.constants.Proxy.PASSWORD,
								fieldLabel: CMDBuild.Translation.password,
								allowBlank: false,
								inputType: 'password'
							},
							Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.LOOKUP_CATEGORY,
								fieldLabel: CMDBuild.Translation.cmdbuildCategory,
								valueField: CMDBuild.core.constants.Proxy.TYPE,
								displayField: CMDBuild.core.constants.Proxy.TYPE,
								allowBlank: false,

								store: CMDBuild.Cache.getLookupTypeLeavesAsStore(),
								queryMode: 'local'
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationAlfrescoTabShow');
			}
		}
	});

})();