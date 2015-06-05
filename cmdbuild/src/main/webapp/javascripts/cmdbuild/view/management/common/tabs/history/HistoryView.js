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
		title: CMDBuild.Translation.history,

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onTabHistoryPanelShow');
			}
		},

		/**
		 * Service function executed from module controller
		 */
		reset: function() {
			this.setDisabled(Ext.isEmpty(this.delegate.cmfg('tabHistorySelectedEntityGet')));
		}
	});

})();