(function() {

	Ext.define('CMDBuild.view.administration.accordion.Filter', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: CMDBuild.Translation.searchFilters,

		constructor: function() {
			this.callParent(arguments);

			this.updateStore();
		},

		/**
		 * @override
		 */
		updateStore: function() {
			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild([
				{
					id: 'groups',
					cmName: 'filter',
					text: CMDBuild.Translation.filtersForGroups,
					leaf: true
				}
			]);
		}
	});

})();