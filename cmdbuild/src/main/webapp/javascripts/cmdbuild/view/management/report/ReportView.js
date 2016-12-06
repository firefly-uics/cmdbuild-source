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

		border: true,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				tools: [
					Ext.create('CMDBuild.controller.common.panel.gridAndForm.tools.properties.Properties', { withSpacer: false }).getView()
				]
			});

			this.callParent(arguments);
		}
	});

})();