(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.import.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Csv'
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
					Ext.create('Ext.form.field.File', {
						name: 'filecsv',
						fieldLabel: CMDBuild.Translation.csvFile,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						allowBlank: false,
						width: CMDBuild.BIG_FIELD_WIDTH
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.CMProxyConstants.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						width: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Csv.getSeparatorStore(),
						queryMode: 'local'
					}),
					this.importModeCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.CMProxyConstants.MODE,
						fieldLabel: CMDBuild.Translation.mode,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
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