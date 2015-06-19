(function() {

	Ext.define('CMDBuild.view.administration.localizations.advancedTable.AdvancedTableView', {
		extend: 'Ext.tab.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.advancedTable.AdvancedTable}
		 */
		delegate: undefined,

		activeTab: 0,
		bodyCls: 'cmgraypanel-nopadding',
		border: true,
		frame: false
	});

})();