(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.import.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
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
						name: CMDBuild.core.constants.Proxy.FORMAT,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						editable: false,
						allowBlank: false,
						disabled: true,

						value: CMDBuild.core.constants.Proxy.CSV, // Default value

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
						name: CMDBuild.core.constants.Proxy.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						width: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Csv.getSeparatorStore(),
						queryMode: 'local'
					}),
					this.importModeCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.MODE,
						fieldLabel: CMDBuild.Translation.mode,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
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