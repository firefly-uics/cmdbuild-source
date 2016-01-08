(function() {

	Ext.define('CMDBuild.view.administration.localization.common.ExportPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.proxy.localization.Export',
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
		frame: false,

		layout: {
			type: 'vbox',
			align: 'stretch'
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
						name: CMDBuild.core.constants.Proxy.TYPE,
						fieldLabel: '@@ Section',
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						editable: false,
						allowBlank: false,

						value: CMDBuild.core.constants.Proxy.ALL, // Default value

						store: CMDBuild.core.proxy.localization.Localization.getStoreSections(),
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
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						editable: false,
						allowBlank: false,
						disabled: true,

						value: CMDBuild.core.constants.Proxy.CSV, // Default value

						store: CMDBuild.core.proxy.localization.Export.getStoreFileFormat(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: 200,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						editable: false,
						allowBlank: false,

						value: ';', // Default value

						store: CMDBuild.core.proxy.Csv.getStoreSeparator(),
						queryMode: 'local'
					}),
					this.activeOnlyCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE_ONLY,
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