(function() {

	Ext.define('CMDBuild.view.management.dataView.DataViewView', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.dataView.DataView}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.views,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: true,
		frame: false,
		layout: 'fit',

		tools: [
			Ext.create('CMDBuild.controller.common.panel.gridAndForm.tools.properties.Properties', { parentDelegate: this }).getView(),
			Ext.create('CMDBuild.view.management.dataView.tools.Minimize'),
			Ext.create('CMDBuild.view.management.dataView.tools.Maximize'),
			Ext.create('CMDBuild.view.management.dataView.tools.Restore')
		]
	});

})();