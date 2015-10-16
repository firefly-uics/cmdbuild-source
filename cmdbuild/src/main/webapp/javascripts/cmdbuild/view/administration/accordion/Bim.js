(function() {

	Ext.define('CMDBuild.view.administration.accordion.Bim', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		disabled: !CMDBuild.configuration.bim.get(CMDBuild.core.constants.Proxy.ENABLED),
		title: CMDBuild.Translation.bim,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					text: CMDBuild.Translation.projects,
					description: CMDBuild.Translation.projects,
					leaf: true,
					cmName: "bim-project"
				},
				{
					text: CMDBuild.Translation.layers,
					description: CMDBuild.Translation.layers,
					leaf: true,
					cmName: "bim-layers"
				}
			]);

			this.callParent(arguments);
		}
	});

})();