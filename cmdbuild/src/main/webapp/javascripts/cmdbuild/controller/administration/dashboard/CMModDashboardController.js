(function() {

	Ext.define("CMDBuild.controller.administration.dashboard.CMModDashboardController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			modDashboardDelegate: "CMDBuild.view.administration.dashboard.CMModDashboardDelegate"
		},

		statics: {
			cmcreate: function(view) {
				var propertiesPanelController = Ext.createByAlias('controller.cmdashboardproperties', view.getPropertiesPanel());
				var chartsConfigurationController = CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController.cmcreate(view.getChartsConfigurationPanel());

				return new CMDBuild.controller.administration.dashboard.CMModDashboardController(view, propertiesPanelController, chartsConfigurationController);
			}
		},

		constructor: function(view, propertiesPanelController, chartsConfigurationController) {
			this.callParent(arguments);
			this.dashboard = null;
			this.view.setDelegate(this);

			this.subcontrollers = [
				this.propertiesPanelController = propertiesPanelController,
				this.chartsConfigurationController = chartsConfigurationController
			]
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

		// view delegate
		onAddButtonClick: function() {
			this.callMethodForAllSubcontrollers("prepareForAdd", [this.dashboard]);
			_CMMainViewportController.deselectAccordionByName(this.view.cmName);
		}
	});

})();