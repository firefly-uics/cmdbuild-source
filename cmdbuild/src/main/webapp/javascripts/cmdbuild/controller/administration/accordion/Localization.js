(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Localization', {
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
			'accordionLocalizationUpdateStore = accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Localization}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Localization', { delegate: this });

			this.cmfg('accordionLocalizationUpdateStore');
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
		accordionLocalizationUpdateStore: function (parameters) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-localization-icon',
					text: CMDBuild.Translation.configuration,
					description: CMDBuild.Translation.configuration,
					id: this.accordionBuildId('configuration'),
					sectionHierarchy: ['configuration'],
					leaf: true
				},
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-localization-icon',
					text: CMDBuild.Translation.localization,
					description: CMDBuild.Translation.localization,
					id: this.accordionBuildId('advancedTranslationsTable'),
					sectionHierarchy: ['advancedTranslationsTable'],
					leaf: true
				},
				{
					cmName: this.accordionIdentifierGet(),
					iconCls: 'cmdb-tree-localization-icon',
					text: CMDBuild.Translation.importExport,
					description: CMDBuild.Translation.importExport,
					id: this.accordionBuildId('importExport'),
					sectionHierarchy: ['importExport'],
					leaf: true
				}
			]);

			this.accordionUpdateStore(arguments); // Custom callParent implementation
		}
	});

})();
