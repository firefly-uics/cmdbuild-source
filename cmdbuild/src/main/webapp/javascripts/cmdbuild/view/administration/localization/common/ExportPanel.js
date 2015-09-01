(function() {

	Ext.define('CMDBuild.view.administration.localization.common.ExportPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.proxy.localization.Localization'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Advanced}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeOnlyCheckbox: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,
		standardSubmit: true,

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
									this.delegate.cmfg('onLocalizationConfigurationExportButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.Constants.SECTION,
						fieldLabel: '@@ Export section',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.Constants.NAME,
						editable: false,
						allowBlank: false,

						value: CMDBuild.core.proxy.Constants.ALL, // Default value

						store: CMDBuild.core.proxy.localization.Localization.getSectionsStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							change: function(combo, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onLocalizationConfigurationExportSectionChange', newValue);
							}
						}
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

						store: CMDBuild.core.proxy.localization.Localization.getFileFormatStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.Constants.SEPARATOR,
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
					}),
					this.activeOnlyCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.proxy.Constants.ACTIVE_ONLY,
						fieldLabel: '@@ Only active',
						labelWidth: CMDBuild.LABEL_WIDTH,
						inputValue: true,
						uncheckedValue: false
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();