(function() {

	Ext.define('CMDBuild.view.administration.accordion.Localizations', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: '@@ Localizations',

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
					cmName: this.cmName,
					leaf: true,
					text: '@@ Base',
					iconCls: 'cmdbuild-tree-localization-icon'
				},
				{
					id: 'advancedTranslations',
					cmName: this.cmName,
					leaf: true,
					text: '@@ Advanced',
					iconCls: 'cmdbuild-tree-localization-icon'
				},
				{
					id: 'advancedTranslationsTable',
					cmName: this.cmName,
					leaf: true,
					text: '@@ Advanced table',
					iconCls: 'cmdbuild-tree-localization-icon'
				}
			]);
		}
	});

})();