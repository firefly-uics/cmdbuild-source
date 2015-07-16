(function() {

	Ext.define('CMDBuild.view.administration.accordion.DataViews', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: CMDBuild.Translation.views,

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
					id: 'filter',
					cmName: 'dataviews',
					leaf: true,
					text: CMDBuild.Translation.filterView
				},
				{
					id: 'sql',
					cmName: 'dataviews',
					leaf: true,
					text: CMDBuild.Translation.sqlView,
				}
			]);
		}
	});

})();