(function () {

	Ext.define('CMDBuild.view.management.common.tabs.history.HistoryView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.History}
		 */
		delegate: undefined,

//		/**
//		 * @property {CMDBuild.view.management.common.tabs.history.GridPanel}
//		 */
//		grid: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.management.modcard.tabs.history,

//		initComponent: function() {
//			Ext.apply(this, {
//				items: [
//					this.grid = Ext.create('CMDBuild.view.management.common.tabs.history.GridPanel', {
//						delegate: this.delegate
//					})
//				]
//			});
//
//			this.callParent(arguments);
//		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onHistoryTabPanelShow');
			}
		}
	});

})();