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
		view: undefined,

		onViewOnFront: function(node) {
			this.view.setItems(CMDBuild.Constants.customPages.path + node.raw.text);
		}

	});

})();
