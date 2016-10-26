(function () {

	Ext.define('CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Functions'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Functions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		fieldFunction: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.fieldFunction = Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
						name: CMDBuild.core.constants.Proxy.FUNCTION,
						fieldLabel: CMDBuild.Translation.functionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						forceSelection: true,

						store: CMDBuild.proxy.common.field.filter.advanced.configurator.tabs.Functions.getStore(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
