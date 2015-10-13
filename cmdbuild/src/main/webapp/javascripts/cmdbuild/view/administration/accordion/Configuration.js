(function() {

	Ext.define('CMDBuild.view.administration.accordion.Configuration', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.setup,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			var nodes = [{
				text: CMDBuild.Translation.generalOptions,
				cmName: this.cmName,
				sectionHierarchy: ['generalOptions'],
				leaf: true
			}];

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				nodes = Ext.Array.push(nodes, [
					{
						text: CMDBuild.Translation.workflowEngine,
						cmName: this.cmName,
						sectionHierarchy: ['workflow'],
						leaf: true
					},
					{
						text: CMDBuild.Translation.relationGraph,
						cmName: this.cmName,
						sectionHierarchy: ['relationGraph'],
						leaf: true
					},
					{
						text: CMDBuild.Translation.alfresco,
						cmName: this.cmName,
						sectionHierarchy: ['alfresco'],
						leaf: true
					},
					{
						text: CMDBuild.Translation.gis,
						cmName: this.cmName,
						sectionHierarchy: ['gis'],
						leaf: true
					},
					{
						text: CMDBuild.Translation.bim,
						cmName: this.cmName,
						sectionHierarchy: ['bim'],
						leaf: true
					},
					{
						text: CMDBuild.Translation.serverManagement,
						cmName: this.cmName,
						sectionHierarchy: ['server'],
						leaf: true
					}
				]);

			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild(nodes);

			this.callParent(arguments);
		}
	});

})();