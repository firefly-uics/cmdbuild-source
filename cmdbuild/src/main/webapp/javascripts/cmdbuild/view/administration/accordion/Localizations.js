(function() {

	Ext.define('CMDBuild.view.administration.accordion.Localizations', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		cmName: 'localizations',
		title: '@@Localizations',

		constructor: function(){
			this.callParent(arguments);

			this.updateStore();
		},

		/**
		 * @override
		 */
		updateStore: function() {
			this.store.getRootNode().appendChild([
				{
					id: 'baseTranslations',
					cmName: 'localizations',
					leaf: true,
					text: '@@ Base',
					iconCls: 'cmdbuild-tree-localization-icon'
				},
				{
					id: 'advancedTranslations',
					cmName: 'localizations',
					leaf: true,
					text: '@@ Advanced',
					iconCls: 'cmdbuild-tree-localization-icon'
				},
				{
					id: 'advancedTranslationsTable',
					cmName: 'localizations',
					leaf: true,
					text: '@@ Advanced table',
					iconCls: 'cmdbuild-tree-localization-icon'
				}
			]);
		}
	});

})();