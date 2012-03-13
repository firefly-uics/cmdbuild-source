(function () {
	CMDBuild.ServiceProxy.url.Dashboard = {
		save: "",
		list: "services/json/dashboard/read",
		remove: ""
	};

	CMDBuild.ServiceProxy.Dashboard = {
		list: function(p) {
			// p.method = "GET";
			// p.url = urls.read;

			// CMDBuild.ServiceProxy.core.doRequest(p);

			var decoded = {
				success: true,
				dashboards: [{
					id: 1,
					name: "Foo",
					groups: [14],
					description: "Amazing dashboard for amazing people",
					charts: []
				}, {
					id: 2,
					groups: [],
					name: "Bar",
					description: "Cool dashboard for cool people",
					charts: []
				}]
			}

			p.success.call(this, null, null, decoded);
			p.callback.call();
		},

		add: function(dashboardData, success, scope) {
			success.apply(scope);

			dashboardData.id = Math.floor(Math.random() * 1000);
			_CMCache.addDashboard(dashboardData);
		},

		modify: function(dashboardData, success, scope) {
			success.apply(scope);
			_CMCache.modifyDashboard(dashboardData);
		},

		remove: function(dashboardId, success, scope) {
			_CMCache.removeDashboardWithId(dashboardId);
			success.apply(scope);
		}
	};

})();