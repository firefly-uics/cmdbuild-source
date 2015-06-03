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
					cmName: 'filterdataview',
					leaf: true,
					text: CMDBuild.Translation.filterView
				},
				{
					id: 'sql',
					cmName: 'sqldataview',
					leaf: true,
					text: CMDBuild.Translation.sqlView,
				}
			]);
		}
	});

})();