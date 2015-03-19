(function() {

	Ext.define('CMDBuild.view.administration.accordion.Configuration', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		cmName: 'setup',
		hideMode: 'offsets',
		title: CMDBuild.Translation.setup,

		constructor: function() {
			this.callParent(arguments);

			this.updateStore();
		},

		/**
		 * @override
		 */
		updateStore: function() {
			var root = this.getStore().getRootNode();
			root.removeAll();

			var children = [{
				text: CMDBuild.Translation.generalOptions,
				leaf: true,
				cmName: 'modsetupcmdbuild'
			}];

			if (!_CMUIConfiguration.isCloudAdmin()) {
				children = children.concat([
					{
						text: CMDBuild.Translation.workflowEngine,
						leaf: true,
						cmName: 'modsetupworkflow'
					},
					{
						text: CMDBuild.Translation.relationGraph,
						leaf: true,
						cmName: 'modsetupgraph'
					},
					{
						text: CMDBuild.Translation.alfresco,
						leaf: true,
						cmName: 'modsetupalfresco'
					},
					{
						text: CMDBuild.Translation.gis,
						leaf: true,
						cmName: 'modsetupgis'
					},
					{
						text: CMDBuild.Translation.bim,
						leaf: true,
						cmName: 'modsetupbim'
					},
					{
						text: CMDBuild.Translation.serverManagement,
						leaf: true,
						cmName: 'modsetupserver'
					}
				]);
			}

			root.appendChild(children);
		}
	});

})();