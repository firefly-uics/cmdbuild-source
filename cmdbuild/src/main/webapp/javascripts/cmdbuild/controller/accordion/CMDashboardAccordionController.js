(function() {
	Ext.define("CMDBuild.controller.accordion.CMDashboardAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",

		constructor: function(accordion) {
			this.callParent(arguments);
			var events = CMDBuild.cache.CMCacheDashboardFunctions.DASHBOARD_EVENTS;

			_CMCache.on(events.add, updateStoreToSelectNode, this);
			_CMCache.on(events.modify, updateStoreToSelectNode, this);
			_CMCache.on(events.remove, updateStore, this);
		}
	});

	function updateStoreToSelectNode(dashboard) {
		this.updateStoreToSelectNodeWithId(dashboard.getId());
	}

	function updateStore() {
		this.accordion.updateStore();
	}

})();