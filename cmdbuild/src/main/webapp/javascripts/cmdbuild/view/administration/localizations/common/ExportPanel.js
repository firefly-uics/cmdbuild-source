(function() {

	Ext.define('CMDBuild.view.administration.localizations.common.ExportPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.localizations.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Advanced}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel',
		bodyPadding: '0 0 32 0', // Hack to fix panel height to be same as left one
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
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
							Ext.create('CMDBuild.core.buttons.Export', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationsExportButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.ComboBox', {
						name: '@@ exportSection',
						fieldLabel: '@@ Export section',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.localizations.Localizations.getSectionsStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: '@@ exportFormat',
						fieldLabel: '@@ Format',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						editable: false,
						allowBlank: false,

						value: CMDBuild.core.proxy.CMProxyConstants.CSV, // Default value

						store: CMDBuild.core.proxy.localizations.Localizations.getFileFormatStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: '@@ exportSeparator',
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: 200,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						editable: false,
						allowBlank: false,

						value: ';',

						store: CMDBuild.core.proxy.localizations.Localizations.getCsvSeparatorStore(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();