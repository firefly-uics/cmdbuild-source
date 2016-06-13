(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Configuration', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionDeselect',
			'accordionExpand',
			'accordionFirstSelectableNodeSelect',
			'accordionFirtsSelectableNodeGet',
			'accordionNodeByIdExists',
			'accordionNodeByIdGet',
			'accordionNodeByIdSelect',
			'accordionConfigurationUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

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
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Configuration', { delegate: this });

			this.cmfg('accordionConfigurationUpdateStore');
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Number or String} parameters.nodeIdToSelect
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		accordionConfigurationUpdateStore: function (parameters) {
			var nodes = [{
				cmName: this.accordionIdentifierGet(),
				iconCls: 'cmdb-tree-configuration-icon',
				text: CMDBuild.Translation.generalOptions,
				description: CMDBuild.Translation.generalOptions,
				id: this.accordionBuildId('generalOptions'),
				sectionHierarchy: ['generalOptions'],
				leaf: true
			}];

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				nodes = Ext.Array.push(nodes, [
					{
						cmName: this.accordionIdentifierGet(),
						iconCls: 'cmdb-tree-configuration-icon',
						text: CMDBuild.Translation.workflowEngine,
						description: CMDBuild.Translation.workflowEngine,
						id: this.accordionBuildId('workflow'),
						sectionHierarchy: ['workflow'],
						leaf: true
					},
					{
						cmName: this.accordionIdentifierGet(),
						iconCls: 'cmdb-tree-configuration-icon',
						text: CMDBuild.Translation.relationGraph,
						description: CMDBuild.Translation.relationGraph,
						id: this.accordionBuildId('relationGraph'),
						sectionHierarchy: ['relationGraph'],
						leaf: true
					},
					{
						cmName: this.accordionIdentifierGet(),
						iconCls: 'cmdb-tree-configuration-icon',
						text: CMDBuild.Translation.dms,
						description: CMDBuild.Translation.dms,
						id: this.accordionBuildId('dms'),
						sectionHierarchy: ['dms'],
						leaf: true
					},
					{
						cmName: this.accordionIdentifierGet(),
						iconCls: 'cmdb-tree-configuration-icon',
						text: CMDBuild.Translation.gis,
						description: CMDBuild.Translation.gis,
						id: this.accordionBuildId('gis'),
						sectionHierarchy: ['gis'],
						leaf: true
					},
					{
						cmName: this.accordionIdentifierGet(),
						iconCls: 'cmdb-tree-configuration-icon',
						text: CMDBuild.Translation.bim,
						description: CMDBuild.Translation.bim,
						id: this.accordionBuildId('bim'),
						sectionHierarchy: ['bim'],
						leaf: true
					},
					{
						cmName: this.accordionIdentifierGet(),
						iconCls: 'cmdb-tree-configuration-icon',
						text: CMDBuild.Translation.serverManagement,
						description: CMDBuild.Translation.serverManagement,
						id: this.accordionBuildId('server'),
						sectionHierarchy: ['server'],
						leaf: true
					}
				]);

			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild(nodes);

			this.accordionUpdateStore(arguments); // Custom callParent implementation
		}
	});

})();
