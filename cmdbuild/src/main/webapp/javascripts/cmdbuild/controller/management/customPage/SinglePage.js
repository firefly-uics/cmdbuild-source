(function() {

	Ext.define('CMDBuild.controller.management.customPage.SinglePage', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} node
		 */
		onViewOnFront: function(node) {
			if (!Ext.isEmpty(node)) {
				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.view.removeAll();
				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.Constants.customPages.path + node.get(CMDBuild.core.constants.Proxy.TEXT)
					}
				});
			}
		}

	});

})();