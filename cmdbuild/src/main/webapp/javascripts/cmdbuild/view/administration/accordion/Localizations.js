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
					id: 'configurations',
					cmName: this.cmName,
					leaf: true,
					text: '@@ Configurations',
					iconCls: 'cmdbuild-tree-localization-icon'
				},
				{
					id: 'advancedTranslationsTable', // TODO rename
					cmName: this.cmName,
					leaf: true,
					text: '@@ Advanced table',
					iconCls: 'cmdbuild-tree-localization-icon'
				}
			]);
		}
	});

})();