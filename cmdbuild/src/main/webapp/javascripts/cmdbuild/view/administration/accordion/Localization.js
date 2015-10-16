(function() {

	Ext.define('CMDBuild.view.administration.accordion.Localization', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
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
					text: '@@ Configuration',
					iconCls: 'cmdbuild-tree-localization-icon',
					sectionHierarchy: ['configuration'],
					cmName: this.cmName,
					leaf: true
				},
				{
					text: '@@ Advanced table',
					iconCls: 'cmdbuild-tree-localization-icon',
					sectionHierarchy: ['advancedTranslationsTable'],
					cmName: this.cmName,
					leaf: true
				}
			]);

			this.callParent(arguments);
		}
	});

})();