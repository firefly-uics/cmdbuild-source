(function() {

	Ext.define('CMDBuild.view.administration.accordion.Localization', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

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
					id: 'configuration',
					cmName: this.cmName,
					leaf: true,
					text: '@@ Configuration',
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