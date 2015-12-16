(function() {

	Ext.define('CMDBuild.view.management.common.widgets.customForm.import.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Csv',
			'CMDBuild.core.proxy.widgets.CustomForm'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.customForm.Import}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Multiselect}
		 */
		keyAttributesMultiselect: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		modeCombo: undefined,

		frame: true,
		border: false,
		encoding: 'multipart/form-data',

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

						store: CMDBuild.core.proxy.widgets.CustomForm.getStoreImportFileFormat(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.proxy.CMProxyConstants.FILE,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.file,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						allowBlank: false,
						maxWidth: CMDBuild.BIG_FIELD_WIDTH
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
					this.modeCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.CMProxyConstants.MODE,
						fieldLabel: CMDBuild.Translation.mode,
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						value: 'replace',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Csv.getStoreImportMode(
							// Remove add option from store in form layout
							this.delegate.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.proxy.CMProxyConstants.LAYOUT) == 'form' ? ['add'] : null
						),
						queryMode: 'local',

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onWidgetCustomFormImportModeChange');
							}
						}
					}),
					this.keyAttributesMultiselect = Ext.create('CMDBuild.view.common.field.multiselect.Multiselect', {
						name: CMDBuild.core.proxy.CMProxyConstants.KEY_ATTRIBUTES,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + 'CMDBuild.Translation.keyAttributes',
						labelWidth: CMDBuild.LABEL_WIDTH,
						labelAlign: 'right',
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						maxHeight: 300,
						maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH,
						considerAsFieldToDisable: true,
						flex: 1, // Stretch vertically
						allowBlank: false,
						disabled: true,

						store: this.delegate.cmfg('widgetCustomFormModelStoreBuilder'),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();