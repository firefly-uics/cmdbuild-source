(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.import.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.proxy.widgets.CustomForm'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.Import}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		importModeCombo: undefined,

		/**
		 * @cfg {Boolean}
		 */
		modeDisabled: false,

		frame: true,
		border: false,
		encoding: 'multipart/form-data',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.ComboBox', { // Prepared for future implementations
						name: CMDBuild.core.proxy.Constants.FORMAT,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.Constants.NAME,
						editable: false,
						allowBlank: false,
						disabled: true,

						value: CMDBuild.core.proxy.Constants.CSV, // Default value

						store: CMDBuild.core.proxy.widgets.CustomForm.getImportFileFormatStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.File', {
						name: 'filecsv',
						fieldLabel: CMDBuild.Translation.file,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						allowBlank: false,
						width: CMDBuild.BIG_FIELD_WIDTH
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.Constants.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.Constants.VALUE,
						displayField: CMDBuild.core.proxy.Constants.VALUE,
						width: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Csv.getSeparatorStore(),
						queryMode: 'local'
					}),
					this.importModeCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.Constants.MODE,
						fieldLabel: CMDBuild.Translation.mode,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.Constants.VALUE,
						displayField: CMDBuild.core.proxy.Constants.DESCRIPTION,
						width: CMDBuild.MEDIUM_FIELD_WIDTH,
						disabled: this.modeDisabled,
						value: 'replace',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Csv.getImportModeStore(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();