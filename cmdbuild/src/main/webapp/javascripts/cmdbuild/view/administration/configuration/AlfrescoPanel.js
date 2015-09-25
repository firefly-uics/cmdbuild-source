(function() {

	Ext.define('CMDBuild.view.administration.configuration.AlfrescoPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Alfresco}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel',
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
								xtype: 'xcheckbox',
								name: CMDBuild.core.constants.Proxy.ENABLED,
								fieldLabel: CMDBuild.Translation.enabled
							},
							{
								name: 'server.url',
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
								name: 'fileserver.type',
								fieldLabel: CMDBuild.Translation.type,
								allowBlank: false,
								disabled: true
							},
							{
								name: 'fileserver.url',
								fieldLabel: CMDBuild.Translation.host,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'fileserver.port',
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
								name: 'repository.fspath',
								fieldLabel: CMDBuild.Translation.fileServerPath,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								name: 'repository.wspath',
								fieldLabel: CMDBuild.Translation.webServicePath,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: false
							},
							{
								name: 'repository.app',
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
								name: 'credential.user',
								fieldLabel: CMDBuild.Translation.username,
								allowBlank: false
							},
							{
								name: 'credential.password',
								fieldLabel: CMDBuild.Translation.password,
								inputType: 'password',
								allowBlank: false
							},
							Ext.create('Ext.form.field.ComboBox', {
								name: 'category.lookup',
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
		}
	});

})();