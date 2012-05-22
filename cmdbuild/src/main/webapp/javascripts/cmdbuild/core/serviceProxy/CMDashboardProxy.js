(function() {

	CMDBuild.ServiceProxy.url.Dashboard = {
		add : "services/json/dashboard/add",
		modify: "services/json/dashboard/modifybaseproperties",
		list : "services/json/dashboard/list",
		remove : "services/json/dashboard/remove",
		addChart: "services/json/dashboard/addchart",
		modifyChart: "services/json/dashboard/modifychart",
		removeChart: "services/json/dashboard/removechart",
		moveChart: "services/json/dashboard/movechart"
	};

	CMDBuild.ServiceProxy.Dashboard = {
		list : function(p) {
			p.method = "GET";
			p.url = CMDBuild.ServiceProxy.url.Dashboard.list;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		add : function(dashboardConfiguration, success, scope) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: "POST",
				url: CMDBuild.ServiceProxy.url.Dashboard.add,
				params: {
					dashboardConfiguration: Ext.encode(dashboardConfiguration)
				},
				success: function(operation, configuration, decodedResponse) {
					dashboardConfiguration.id = decodedResponse.response;
					_debug(dashboardConfiguration);
					success.apply(scope);

					_CMCache.addDashboard(dashboardConfiguration);
				}
			});
		},

		modify : function(dashboardId, dashboardConfiguration, success, scope) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: "POST",
				url: CMDBuild.ServiceProxy.url.Dashboard.modify,
				params: {
					dashboardId: dashboardId,
					dashboardConfiguration: Ext.encode(dashboardConfiguration)
				},
				success: function(operation, configuration, decodedResponse) {
					 success.apply(scope);

					 _CMCache.modifyDashboard(dashboardConfiguration, dashboardId);
				}
			});
		},

		remove : function(dashboardId, success, scope) {
			CMDBuild.ServiceProxy.core.doRequest({
				method: "POST",
				url: CMDBuild.ServiceProxy.url.Dashboard.remove,
				params: {
					dashboardId: dashboardId
				},
				success: function(operation, configuration, decodedResponse) {
					 success.apply(scope);

					 _CMCache.removeDashboardWithId(dashboardId);
				}
			});
		},

		chart: {
			add: function(dashboardId, chartConfiguration, cb) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: "POST",
					url: CMDBuild.ServiceProxy.url.Dashboard.addChart,
					params: {
						dashboardId: dashboardId,
						chartConfiguration: Ext.encode(chartConfiguration)
					},
					success: function(operation, configuration, decodedResponse) {
						var d = _CMCache.getDashboardById(dashboardId);
						if (d) {
							chartConfiguration.id = decodedResponse.response;
							var chart = CMDBuild.model.CMDashboardChart.build(chartConfiguration);
							d.addChart(chart);

							if (typeof cb == "function") {
								cb(d.getCharts(), chart.getId());
							}
						}
					}
				});
			},

			modify: function(dashboardId, chartId, chartConfiguration, cb) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: "POST",
					url: CMDBuild.ServiceProxy.url.Dashboard.modifyChart,
					params: {
						dashboardId: dashboardId,
						chartId: chartId,
						chartConfiguration: Ext.encode(chartConfiguration)
					},
					success: function(operation, configuration, decodedResponse) {
						var d = _CMCache.getDashboardById(dashboardId);
						if (d) {
							chartConfiguration.id = chartId;
							var chart = CMDBuild.model.CMDashboardChart.build(chartConfiguration);
							d.replaceChart(chartId, chart);

							if (typeof cb == "function") {
								cb(d.getCharts(), chart.getId());
							}
						}
					}
				});
			},

			remove: function(dashboardId, chartId, cb) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: "POST",
					url: CMDBuild.ServiceProxy.url.Dashboard.removeChart,
					params: {
						dashboardId: dashboardId,
						chartId: chartId
					},
					success: function(operation, configuration, decodedResponse) {
						var d = _CMCache.getDashboardById(dashboardId);
						if (d) {
							d.removeChart(chartId);
							if (typeof cb == "function") {
								cb(d.getCharts());
							}
						}
					}
				});
			},

			move: function(fromDashboardId, toDashboardId, chartId, cb) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: "POST",
					url: CMDBuild.ServiceProxy.url.Dashboard.moveChart,
					params: {
						fromDashboardId: fromDashboardId,
						toDashboardId: toDashboardId,
						chartId: chartId
					},
					success: function(operation, configuration, decodedResponse) {
						var fromDashboard = _CMCache.getDashboardById(fromDashboardId),
							toDashboard = _CMCache.getDashboardById(toDashboardId),
							chart = null;

						if (fromDashboard) {
							chart = fromDashboard.getChartWithId(chartId);
							fromDashboard.removeChart(chartId);
						}
		
						if (toDashboard && chart) {
							toDashboard.addChart(chart);
						}
		
						if (typeof cb == "function") {
							cb();
						}
					}
				});
			}
		}
	};

})();
