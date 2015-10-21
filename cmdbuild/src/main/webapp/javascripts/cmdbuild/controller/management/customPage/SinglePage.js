(function() {

	Ext.define('CMDBuild.controller.management.customPage.SinglePage', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.Global'
		],

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
				var basePath = window.location.toString().split('/');
				basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.view.removeAll();
				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.Constants.customPages.customizationsPath
							+ node.get(CMDBuild.core.constants.Proxy.TEXT)
							+ '/?basePath=' + basePath
							+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPages.getVersion()
					}
				});
			}
		}

	});

})();