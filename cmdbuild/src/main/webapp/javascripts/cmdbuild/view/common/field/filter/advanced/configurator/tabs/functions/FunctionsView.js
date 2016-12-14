(function () {

	Ext.define('CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FunctionsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.Functions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		hidden: true,
		layout: 'fit',
		title: CMDBuild.Translation.functionLabel,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.functions.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
