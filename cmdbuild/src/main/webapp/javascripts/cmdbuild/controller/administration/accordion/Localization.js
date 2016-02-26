(function() {

	Ext.define('CMDBuild.controller.administration.accordion.Localization', {
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
		 * @property {CMDBuild.view.administration.accordion.Localization}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Localization', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdbuild-tree-localization-icon',
					text: CMDBuild.Translation.configuration,
					description: CMDBuild.Translation.configuration,
					id: this.cmfg('accordionBuildId', { components: 'configuration' }),
					sectionHierarchy: ['configuration'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdbuild-tree-localization-icon',
					text: CMDBuild.Translation.importExport,
					description: CMDBuild.Translation.importExport,
					id: this.cmfg('accordionBuildId', { components: 'importExport' }),
					sectionHierarchy: ['importExport'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdbuild-tree-localization-icon',
					text: CMDBuild.Translation.bulkUpdate,
					description: CMDBuild.Translation.bulkUpdate,
					id: this.cmfg('accordionBuildId', { components: 'advancedTranslationsTable' }),
					sectionHierarchy: ['advancedTranslationsTable'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();
