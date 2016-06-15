(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Bim', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

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
			'accordionBimUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Bim}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Bim', { delegate: this });

			this.cmfg('accordionBimUpdateStore');
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
		accordionBimUpdateStore: function (parameters) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: 'bim-project',
					iconCls: 'cmdb-tree-bim-icon',
					text: CMDBuild.Translation.projects,
					description: CMDBuild.Translation.projects,
					id: this.accordionBuildId('bim-project'),
					sectionHierarchy: ['bim-project'],
					leaf: true
				},
				{
					cmName: 'bim-layers',
					iconCls: 'cmdb-tree-bim-icon',
					text: CMDBuild.Translation.layers,
					description: CMDBuild.Translation.layers,
					id: this.accordionBuildId('bim-layers'),
					sectionHierarchy: ['bim-layers'],
					leaf: true
				}
			]);

			this.accordionUpdateStore(arguments); // Custom callParent implementation
		}
	});

})();
