(function() {

	Ext.define('CMDBuild.view.management.reports.ReportsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.reports.Reports}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.reports.GridPanel}
		 */
		grid: undefined,

		border: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.report
	});

})();