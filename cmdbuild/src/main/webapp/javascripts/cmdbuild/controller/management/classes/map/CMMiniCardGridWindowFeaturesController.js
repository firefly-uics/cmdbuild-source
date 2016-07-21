(function() {
	Ext.define("CMDBuild.controller.management.CMMiniCardGridWindowController", {
		mixins: {
			cmMiniCardGridWindowDelegate: "CMDBuild.view.management.CMMiniCardGridWindowDelegate",
			cmMiniCardGridDelegate: "CMDBuild.view.management.CMMiniCardGridDelegate"
		},

		miniGridWindow: undefined,

		constructor: function() {
			this.dataSource = new CMDBuild.data.CMDetailedCardDataSource();

			this.callParent(arguments);
		},

		bindMiniCardGridWindow: function(miniGridWindow) {
			this.miniGridWindow = miniGridWindow;
			this.miniGridWindow.addDelegate(this);

			this.miniGrid = this.miniGridWindow.getMiniCardGrid();
			this.miniGrid.addDelegate(this);

			this.dataSource.clearStore();
		},

		getDataSource: function() {
			return this.dataSource;
		},

		// * As CMMiniCardGridDelegate ********************/

		miniCardGridItemSelected: function(grid, card) {
			this.miniGridWindow.showDetailsForCard(card);
		},

		miniCardGridWantOpenCard: function(grid, card) {
		},

		// * As CMMiniCardGridWindowDelegate *******************/

		miniCardGridWindowDidShown: function(grid) {}
	});

	Ext.define("CMDBuild.controller.management.CMMiniCardGridWindowFeaturesController", {

		extend: "CMDBuild.controller.management.CMMiniCardGridWindowController",

		features: [],

		constructor: function() {
			this.callParent(arguments);
		},


		setFeatures: function(features) {
			this.features = features || [];
		},

		// override
		miniCardGridWindowDidShown: function(grid) {
		}
	});
})();