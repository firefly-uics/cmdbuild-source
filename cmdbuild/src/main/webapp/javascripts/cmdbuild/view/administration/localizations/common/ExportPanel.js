(function() {

	Ext.define('CMDBuild.view.administration.localizations.common.ExportPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Csv',
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
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Export', {
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
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.Constants.NAME,
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
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.Constants.NAME,
						editable: false,
						allowBlank: false,

						value: CMDBuild.core.proxy.Constants.CSV, // Default value

						store: CMDBuild.core.proxy.localizations.Localizations.getFileFormatStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: '@@ exportSeparator',
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: 200,
						valueField: CMDBuild.core.proxy.Constants.VALUE,
						displayField: CMDBuild.core.proxy.Constants.VALUE,
						editable: false,
						allowBlank: false,

						value: ';', // Default value

						store: CMDBuild.core.proxy.Csv.getSeparatorStore(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();