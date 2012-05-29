Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardLayoutPanelController", {

	alias: ["controller.cmdashboardlayoutconf"],

	mixins: {
		columnController: "CMDBuild.controller.common.CMDashboardColumnController", // the order is important
		viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardLayoutPanelDelegate"
	},

	constructor: function(view) {
		this.view = view;
		this.view.setDelegate(this);
		this.dashboard = null;
		this.proxy = CMDBuild.ServiceProxy.Dashboard;
	},

	dashboardWasSelected: function(dashboard) {
		this.dashboard = dashboard;
		if (this.view.isTheActiveTab()) {
			this.view.configureForDashboard(dashboard);
		} else {
			var me = this;
			this.view.mon(this.view, "activate", function() {
				me.view.configureForDashboard(dashboard);
			}, {
				single: true
			});
		}
	},

	// view delegate

	onAddColumnClick: function() {
		var actualColumnCount = this.view.countColumns();
		var factor = 1/(actualColumnCount + 1);

		this.view.addColumn({
			charts: [],
			width: factor
		});
	},

	onRemoveColumnClick: function() {
		this.view.removeEmptyColumns();
	},

	onColumnWidthSliderChange: function() {
		this.view.syncColumnWidthToSliderThumbs();
	},

	onSaveButtonClick: function() {
		this.proxy.modifyColumns(this.dashboard.getId(), this.view.getColumnsConfiguration());
	},

	onAbortButtonClick: function() {
		this.dashboardWasSelected(this.dashboard);
	}
});