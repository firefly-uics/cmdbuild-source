(function() {

	Ext.define('CMDBuild.view.administration.accordion.Localization', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.abstract.Accordion}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		title: CMDBuild.Translation.localizations,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					cmName: this.cmName,
					iconCls: 'cmdbuild-tree-localization-icon',
					text: CMDBuild.Translation.configuration,
					description: CMDBuild.Translation.configuration,
					id: this.delegate.cmfg('accordionBuildId', { components: 'configuration' }),
					sectionHierarchy: ['configuration'],
					leaf: true
				},
				{
					cmName: this.cmName,
					iconCls: 'cmdbuild-tree-localization-icon',
					text: CMDBuild.Translation.importExport,
					description: CMDBuild.Translation.importExport,
					id: this.delegate.cmfg('accordionBuildId', { components: 'importExport' }),
					sectionHierarchy: ['importExport'],
					leaf: true
				},
				{
					cmName: this.cmName,
					iconCls: 'cmdbuild-tree-localization-icon',
					text: CMDBuild.Translation.advancedTables,
					description: CMDBuild.Translation.advancedTables,
					id: this.delegate.cmfg('accordionBuildId', { components: 'advancedTranslationsTable' }),
					sectionHierarchy: ['advancedTranslationsTable'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();