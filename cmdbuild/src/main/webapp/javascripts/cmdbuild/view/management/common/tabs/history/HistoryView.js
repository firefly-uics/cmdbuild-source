(function () {

	Ext.define('CMDBuild.view.management.common.tabs.history.HistoryView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.History}
		 */
		delegate: undefined,

		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.management.modcard.tabs.history,

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onHistoryTabPanelShow');
			}
		}
	});

})();