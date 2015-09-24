(function() {

	Ext.define('CMDBuild.view.management.report.ReportView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.report.Report}
		 */
		delegate: undefined,

		border: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.report
	});

})();