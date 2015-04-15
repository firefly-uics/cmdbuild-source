(function() {

	Ext.define('CMDBuild.view.management.report.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.report.SingleReport}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.report.GridPanel}
		 */
		grid: undefined,

		border: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.report,

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.management.report.GridPanel', {
				delegate: this.delegate
			});

			Ext.apply(this, {
				items: [this.grid]
			});

			this.callParent(arguments);
		}
	});

})();