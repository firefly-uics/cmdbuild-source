(function() {

	Ext.define('CMDBuild.view.administration.accordion.Configuration', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: CMDBuild.Translation.setup,

		constructor: function() {
			this.callParent(arguments);

			this.updateStore();
		},

		/**
		 * @override
		 */
		updateStore: function() {
			this.store.getRootNode().appendChild([{
				id: 'generalOptions',
				text: CMDBuild.Translation.generalOptions,
				leaf: true,
				cmName: 'configuration'
			}]);

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				this.store.getRootNode().appendChild([
					{
						id: 'workflow',
						text: CMDBuild.Translation.workflowEngine,
						leaf: true,
						cmName: 'configuration'
					},
					{
						id: 'relationGraph',
						text: CMDBuild.Translation.relationGraph,
						leaf: true,
						cmName: 'configuration'
					},
					{
						id: 'alfresco',
						text: CMDBuild.Translation.alfresco,
						leaf: true,
						cmName: 'configuration'
					},
					{
						id: 'gis',
						text: CMDBuild.Translation.gis,
						leaf: true,
						cmName: 'configuration'
					},
					{
						id: 'bim',
						text: CMDBuild.Translation.bim,
						leaf: true,
						cmName: 'configuration'
					},
					{
						id: 'server',
						text: CMDBuild.Translation.serverManagement,
						leaf: true,
						cmName: 'configuration'
					}
				]);
		}
	});

})();