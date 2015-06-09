(function() {

	Ext.define('CMDBuild.controller.management.guiFramework.OnePanelFrame', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		/**
		 * @property {Object}
		 */
		displayedReportParams: undefined,

		view: undefined,

		onViewOnFront: function(node) {
			this.view.setItems(node.raw.src);
			console.log("this.view " + node.raw.src, this.view);
		}

	});

})();
