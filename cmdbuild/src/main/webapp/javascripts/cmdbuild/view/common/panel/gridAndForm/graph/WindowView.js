(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.graph.WindowView', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.graph.Window}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.relationGraph,

		listeners: {
			show: function (window, eOpts) {
				this.delegate.cmfg('onPanelGridAndFormGraphWindowShow');
			}
		}
	});

})();
