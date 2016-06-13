(function () {

	Ext.define('CMDBuild.controller.administration.accordion.DataView', {
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
			'accordionDataViewUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.DataView}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.DataView', { delegate: this });

			this.cmfg('accordionDataViewUpdateStore');
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
		accordionDataViewUpdateStore: function (parameters) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.accordionIdentifierGet(),
					text: CMDBuild.Translation.filterView,
					description: CMDBuild.Translation.filterView,
					id: this.accordionBuildId('filter'),
					sectionHierarchy: ['filter'],
					leaf: true
				},
				{
					cmName: this.accordionIdentifierGet(),
					text: CMDBuild.Translation.sqlView,
					description: CMDBuild.Translation.sqlView,
					id: this.accordionBuildId('sql'),
					sectionHierarchy: ['sql'],
					leaf: true
				}
			]);

			this.accordionUpdateStore(arguments); // Custom callParent implementation
		}
	});

})();
