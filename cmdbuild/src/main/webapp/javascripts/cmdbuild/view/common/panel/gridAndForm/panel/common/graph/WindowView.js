(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.panel.common.graph.WindowView', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.panel.common.graph.Window}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		dimensionsMode: 'percentage',

		border: true,
		closeAction: 'hide',
		frame: false,
		title: CMDBuild.Translation.relationGraph
	});

})();
