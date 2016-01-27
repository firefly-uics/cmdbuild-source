(function() {

	Ext.define('CMDBuild.view.administration.localization.importExport.ExportForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.proxy.localization.Export',
			'CMDBuild.core.proxy.localization.Localization'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.ImportExport}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,
		title: CMDBuild.Translation.exportLabel,

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
									this.delegate.cmfg('onLocalizationImportExportExportButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.TYPE,
						fieldLabel: CMDBuild.Translation.section,
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
								this.delegate.cmfg('onLocalizationImportExportExportSectionChange', newValue);
							}
						}
					}),
					Ext.create('CMDBuild.view.common.field.multiselect.Multiselect', {
						name: CMDBuild.core.constants.Proxy.LANGUAGES, // TODO: synch with server side parameter name
						fieldLabel: CMDBuild.Translation.languages,
						labelWidth: CMDBuild.LABEL_WIDTH,
						valueField: CMDBuild.core.constants.Proxy.TAG,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxHeight: 300,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						considerAsFieldToDisable: true,
						defaultSelection: 'all',
						flex: 1, // Stretch vertically
						allowBlank: false,

						store: CMDBuild.core.proxy.localization.Localization.getStoreLanguages(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.FORMAT,
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
						fieldLabel: CMDBuild.Translation.activeOnly,
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