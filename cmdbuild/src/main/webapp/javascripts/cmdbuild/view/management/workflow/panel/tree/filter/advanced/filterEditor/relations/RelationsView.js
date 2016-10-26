(function () {

	/**
	 * @link CMDBuild.view.common.field.filter.advanced.configurator.tabs.relations.RelationsView
	 */
	Ext.define('CMDBuild.view.management.workflow.panel.tree.filter.advanced.filterEditor.relations.RelationsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.tree.filter.advanced.filterEditor.relations.Relations}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.relations
	});

})();
