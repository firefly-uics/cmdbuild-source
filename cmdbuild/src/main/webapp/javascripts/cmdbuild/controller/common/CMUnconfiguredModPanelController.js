(function() {
	Ext.define("CMDBuild.controller.common.CMUnconfiguredModPanelController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		onViewOnFront: function() {
			this.view.update(arguments[0]);
		}
	});
})();