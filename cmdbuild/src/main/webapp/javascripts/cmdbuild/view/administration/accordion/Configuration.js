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
				cmName: this.cmName,
				text: CMDBuild.Translation.generalOptions,
				description: CMDBuild.Translation.generalOptions,
				id: this.delegate.cmfg('accordionBuildId', { components: 'generalOptions' }),
				sectionHierarchy: ['generalOptions'],
				leaf: true
			}];

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				nodes = Ext.Array.push(nodes, [
					{
						cmName: this.cmName,
						text: CMDBuild.Translation.workflowEngine,
						description: CMDBuild.Translation.workflowEngine,
						id: this.delegate.cmfg('accordionBuildId', { components: 'workflow' }),
						sectionHierarchy: ['workflow'],
						leaf: true
					},
					{
						cmName: this.cmName,
						text: CMDBuild.Translation.relationGraph,
						description: CMDBuild.Translation.relationGraph,
						id: this.delegate.cmfg('accordionBuildId', { components: 'relationGraph' }),
						sectionHierarchy: ['relationGraph'],
						leaf: true
					},
					{
						cmName: this.cmName,
						text: CMDBuild.Translation.alfresco,
						description: CMDBuild.Translation.alfresco,
						id: this.delegate.cmfg('accordionBuildId', { components: 'alfresco' }),
						sectionHierarchy: ['alfresco'],
						leaf: true
					},
					{
						cmName: this.cmName,
						text: CMDBuild.Translation.gis,
						description: CMDBuild.Translation.gis,
						id: this.delegate.cmfg('accordionBuildId', { components: 'gis' }),
						sectionHierarchy: ['gis'],
						leaf: true
					},
					{
						cmName: this.cmName,
						text: CMDBuild.Translation.bim,
						description: CMDBuild.Translation.bim,
						id: this.delegate.cmfg('accordionBuildId', { components: 'bim' }),
						sectionHierarchy: ['bim'],
						leaf: true
					},
					{
						cmName: this.cmName,
						text: CMDBuild.Translation.serverManagement,
						description: CMDBuild.Translation.serverManagement,
						id: this.delegate.cmfg('accordionBuildId', { components: 'server' }),
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