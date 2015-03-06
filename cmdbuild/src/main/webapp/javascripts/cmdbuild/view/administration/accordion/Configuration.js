(function() {

	var tr = CMDBuild.Translation.administration.setup;

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
						text: tr.workflow.menuTitle,
						leaf: true,
						cmName: 'modsetupworkflow'
					},
					{
						text: tr.graph.menuTitle,
						leaf: true,
						cmName: 'modsetupgraph'
					},
					{
						text: tr.dms.menuTitle,
						leaf: true,
						cmName: 'modsetupalfresco'
					},
					{
						text: tr.gis.title,
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