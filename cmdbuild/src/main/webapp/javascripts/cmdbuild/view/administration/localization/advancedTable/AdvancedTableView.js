(function() {

	Ext.define('CMDBuild.view.administration.localization.advancedTable.AdvancedTableView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		delegate: undefined,

		activeTab: 0,
		bodyCls: 'cmgraypanel-nopadding',
		border: true,
		frame: false
	});

})();