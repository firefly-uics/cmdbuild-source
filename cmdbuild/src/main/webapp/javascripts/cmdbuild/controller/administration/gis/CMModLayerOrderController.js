(function() {

	Ext.define("CMDBuild.controller.administration.gis.CMModLayerOrderController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
		},

		onViewOnFront: function() {
			this.view.store.load();
		}
	});
})();