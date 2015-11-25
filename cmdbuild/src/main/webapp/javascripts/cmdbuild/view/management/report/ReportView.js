(function() {

	Ext.define('CMDBuild.view.management.report.ReportView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.report.Report}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.report,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		border: true,
		frame: false,
		layout: 'fit'
	});

})();