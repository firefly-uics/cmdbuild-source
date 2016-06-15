(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Email', {
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
			'accordionEmailUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Email}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Email', { delegate: this });

			this.cmfg('accordionEmailUpdateStore');
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
		accordionEmailUpdateStore: function (parameters) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-email-icon',
					text: CMDBuild.Translation.accounts,
					description: CMDBuild.Translation.accounts,
					id: this.accordionBuildId('accounts'),
					sectionHierarchy: ['accounts'],
					leaf: true
				},
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-email-icon',
					text: CMDBuild.Translation.templates,
					description: CMDBuild.Translation.templates,
					id: this.accordionBuildId('templates'),
					sectionHierarchy: ['templates'],
					leaf: true
				},
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-email-icon',
					text: CMDBuild.Translation.queue,
					description: CMDBuild.Translation.queue,
					id: this.accordionBuildId('queue'),
					sectionHierarchy: ['queue'],
					leaf: true
				}
			]);

			this.accordionUpdateStore(arguments); // Custom callParent implementation
		}
	});

})();
