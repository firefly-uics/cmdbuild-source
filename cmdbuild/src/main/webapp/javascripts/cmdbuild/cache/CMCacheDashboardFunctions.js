(function() {

	var dashboards = {},
		events = {
			add: "cm-dashboard-added",
			remove: "cm-dashboard-removed",
			modify: "cm-dashboard-modify"
		};

	Ext.define("CMDBuild.cache.CMCacheDashboardFunctions", {
		statics: {
			DASHBOARD_EVENTS: events
		},

		DASHBOARD_EVENTS: events,

		addDashboards: function(dd) {
			for (var i=0, l=dd.length; i<l; ++i) {
				this.addDashboard(dd[i]);
			}
		},

		addDashboard: function(d) {
			var model = Ext.create("CMDBuild.model.CMDashboard", d);
			if (model) {
				dashboards[d.id] = model;
				this.fireEvent(this.DASHBOARD_EVENTS.add, model);
			}
		},

		removeDashboardWithId: function(id) {
			var d = dashboards[id];
			if (d) {
				delete dashboards[id];
				this.fireEvent(this.DASHBOARD_EVENTS.remove, id);
			}
		},

		modifyDashboard: function(dashboard) {
			var d = dashboards[dashboard.id];
			if (d) {
				d.setName(dashboard.name);
				d.setDescription(dashboard.description);
				d.setGroups(dashboard.groups);
				this.fireEvent(this.DASHBOARD_EVENTS.modify, d);
			}
		},

		getDashboards: function() {
			return dashboards;
		},

		getDashboardById: function(id) {
			return dashboards[id] || null;
		}
	});

})();