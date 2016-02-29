(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Configuration', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Configuration}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Configuration', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			var nodes = [{
				cmName: this.cmfg('accordionIdentifierGet'),
				text: CMDBuild.Translation.generalOptions,
				description: CMDBuild.Translation.generalOptions,
				id: this.cmfg('accordionBuildId', { components: 'generalOptions' }),
				sectionHierarchy: ['generalOptions'],
				leaf: true
			}];

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				nodes = Ext.Array.push(nodes, [
					{
						cmName: this.cmfg('accordionIdentifierGet'),
						text: CMDBuild.Translation.workflowEngine,
						description: CMDBuild.Translation.workflowEngine,
						id: this.cmfg('accordionBuildId', { components: 'workflow' }),
						sectionHierarchy: ['workflow'],
						leaf: true
					},
					{
						cmName: this.cmfg('accordionIdentifierGet'),
						text: CMDBuild.Translation.relationGraph,
						description: CMDBuild.Translation.relationGraph,
						id: this.cmfg('accordionBuildId', { components: 'relationGraph' }),
						sectionHierarchy: ['relationGraph'],
						leaf: true
					},
					{
						cmName: this.cmfg('accordionIdentifierGet'),
						text: CMDBuild.Translation.alfresco,
						description: CMDBuild.Translation.alfresco,
						id: this.cmfg('accordionBuildId', { components: 'alfresco' }),
						sectionHierarchy: ['alfresco'],
						leaf: true
					},
					{
						cmName: this.cmfg('accordionIdentifierGet'),
						text: CMDBuild.Translation.gis,
						description: CMDBuild.Translation.gis,
						id: this.cmfg('accordionBuildId', { components: 'gis' }),
						sectionHierarchy: ['gis'],
						leaf: true
					},
					{
						cmName: this.cmfg('accordionIdentifierGet'),
						text: CMDBuild.Translation.bim,
						description: CMDBuild.Translation.bim,
						id: this.cmfg('accordionBuildId', { components: 'bim' }),
						sectionHierarchy: ['bim'],
						leaf: true
					},
					{
						cmName: this.cmfg('accordionIdentifierGet'),
						text: CMDBuild.Translation.serverManagement,
						description: CMDBuild.Translation.serverManagement,
						id: this.cmfg('accordionBuildId', { components: 'server' }),
						sectionHierarchy: ['server'],
						leaf: true
					}
				]);

			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild(nodes);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();