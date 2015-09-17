(function() {

	Ext.define('CMDBuild.view.administration.accordion.DataView', {
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
					cmName: 'dataview',
					leaf: true,
					text: CMDBuild.Translation.filterView
				},
				{
					id: 'sql',
					cmName: 'dataview',
					leaf: true,
					text: CMDBuild.Translation.sqlView,
				}
			]);
		}
	});

})();