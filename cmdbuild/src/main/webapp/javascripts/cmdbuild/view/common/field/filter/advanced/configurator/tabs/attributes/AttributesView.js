(function () {

	Ext.define('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.AttributesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.attributes.Attributes}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		hidden: true,
		layout: 'fit',
		title: CMDBuild.Translation.attributes,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.field.filter.advanced.configurator.tabs.attributes.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
