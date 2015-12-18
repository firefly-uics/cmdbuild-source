(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.export.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.proxy.widgets.CustomForm'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.Export}
		 */
		delegate: undefined,

		frame: true,
		border: false,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.ComboBox', { // Prepared for future implementations
						name: CMDBuild.core.proxy.CMProxyConstants.FORMAT,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						editable: false,
						allowBlank: false,
						disabled: true,

						value: CMDBuild.core.proxy.CMProxyConstants.CSV, // Default value

						store: CMDBuild.core.proxy.widgets.CustomForm.getStoreExportFileFormat(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.proxy.CMProxyConstants.FILE_NAME,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.fileName,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						maxWidth: CMDBuild.BIG_FIELD_WIDTH,
						allowBlank: false,
						vtype: 'alphanumextended'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.CMProxyConstants.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						maxWidth: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Csv.getStoreSeparator(),
						queryMode: 'local'
					}),
					Ext.create('CMDBuild.view.common.field.multiselect.Multiselect', {
						name: CMDBuild.core.proxy.CMProxyConstants.HEADERS,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.dataToExport,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						maxHeight: 300,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						considerAsFieldToDisable: true,
						defaultSelection: 'all',
						flex: 1, // Stretch vertically
						allowBlank: false,

						store: this.delegate.cmfg('widgetCustomFormModelStoreBuilder'),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();