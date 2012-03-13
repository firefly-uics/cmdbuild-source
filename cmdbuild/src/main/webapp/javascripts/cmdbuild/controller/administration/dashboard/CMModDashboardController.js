(function() {

	Ext.define("CMDBuild.controller.administration.dashboard.CMModDashboardController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			modDashboardDelegate: "CMDBuild.view.administration.dashboard.CMModDashboardDelegate"
		},

		constructor: function(view) {
			this.callParent(arguments);
			this.dashboard = null;
			this.initSubControllers(view);
			this.view.setDelegate(this);
		},

		onViewOnFront: function(relatedTreeNode) {
			var title = null;
			this.dashboard = null;

			if (relatedTreeNode) {
				var id = relatedTreeNode.get("id");
				this.dashboard = _CMCache.getDashboardById(id);
			}

			if (this.dashboard) {
				this.view.setTitleSuffix(this.dashboard.get("description"));
				this.callMethodForAllSubcontrollers("dashboardWasSelected", [this.dashboard])
			}
		},

		initSubControllers: function(view) {
			this.propertiesPanelController = new CMDBuild.controller.administration
			.dashboard.CMDashboardPropertiesPanelController(view.getPropertiesPanel());

			this.subcontrollers = [
				this.propertiesPanelController
			]
		},

		// view delegate
		onAddButtonClick: function() {
			this.callMethodForAllSubcontrollers("prepareForAdd", [this.dashboard]);
			_CMMainViewportController.deselectAccordionByName(this.view.cmName);
		}
	});

})();