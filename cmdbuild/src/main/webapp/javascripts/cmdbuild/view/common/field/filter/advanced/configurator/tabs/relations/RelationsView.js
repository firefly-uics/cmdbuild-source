(function () {

	Ext.define('CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.RelationsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.configurator.tabs.relations.Relations}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		hidden: true,
		layout: 'border',
		title: CMDBuild.Translation.relations
	});

})();
