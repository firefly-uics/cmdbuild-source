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

		title: '@@ Localizations',

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
					text: '@@ Configuration',
					description: '@@ Configuration',
					id: this.delegate.cmfg('accordionBuildId', { components: 'configuration' }),
					sectionHierarchy: ['configuration'],
					leaf: true
				},
				{
					cmName: this.cmName,
					iconCls: 'cmdbuild-tree-localization-icon',
					text: '@@ Advanced tables',
					description: '@@ Advanced tables',
					id: this.delegate.cmfg('accordionBuildId', { components: 'advancedTranslationsTable' }),
					sectionHierarchy: ['advancedTranslationsTable'],
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();