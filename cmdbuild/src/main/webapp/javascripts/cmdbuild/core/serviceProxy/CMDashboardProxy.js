(function() {
	CMDBuild.ServiceProxy.url.Dashboard = {
		save : "",
		list : "services/json/dashboard/read",
		remove : ""
	};

	CMDBuild.ServiceProxy.Dashboard = {
		list : function(p) {
			// p.method = "GET";
			// p.url = urls.read;

			// CMDBuild.ServiceProxy.core.doRequest(p);

			var decoded = {
				success : true,
				response: {
					dashboards : [{
						id : 1,
						name : "Foo",
						groups : [14],
						description : "Amazing dashboard for amazing people",
						charts : [{
							id : 1,
							name : "Card opened per group",
							description : "A pie chart with the number of opened card in a year, divided per group",
							type: "gauge",
							dataSource: {
								name: 'cm_datasource_1',
								input: [{
									name: 'in11',
									type: 'integer',
									fieldType: 'free',
									defaultValue: 545
								}]
							},
							minimum: 10,
							maximum: 1000,
							steps: 20,
							singleSerieField: "out11"
						}, {
							id : 2,
							name : "Card opened per class",
							description : "A pie chart with the number of opened card in a year, divided per class"
						}]
					}, {
						id : 2,
						groups : [],
						name : "Bar",
						description : "Cool dashboard for cool people",
						charts : []
					}],

					dataSources: [{
						name: "cm_datasource_1",
						input: [{
							name: "in11",
							type: "integer"
						},{
							name: "in12",
							type: "string"
						},{
							name: "in13",
							type: "date"
						}],
						output: [{
							name: "out11",
							type: "integer"
						},{
							name: "out12",
							type: "string"
						},{
							name: "out13",
							type: "date"
						}]
					}, {
						name: "cm_datasource_2",
						input: [{
							name: "in21",
							type: "integer"
						},{
							name: "in22",
							type: "string"
						},{
							name: "in23",
							type: "date"
						}],
						output: [{
							name: "out21",
							type: "integer"
						},{
							name: "out22",
							type: "string"
						},{
							name: "out23",
							type: "date"
						}]
					}]
				}
			}

			p.success.call(this, null, null, decoded);
			p.callback.call();
		},

		add : function(dashboardData, success, scope) {
			// success.apply(scope);
// 
			// dashboardData.id = Math.floor(Math.random() * 1000);
			// _CMCache.addDashboard(dashboardData);
		},

		modify : function(dashboardData, success, scope) {
			// success.apply(scope);
			// _CMCache.modifyDashboard(dashboardData);
		},

		remove : function(dashboardId, success, scope) {
			// _CMCache.removeDashboardWithId(dashboardId);
			// success.apply(scope);
		},

		chart: {
			add: function(dashboardId, chartData, cb) {
				var d = _CMCache.getDashboardById(dashboardId);
				if (d) {
					var chart = CMDBuild.model.CMDashboardChart.build(chartData);
					d.addChart(chart);
					if (typeof cb == "function") {
						cb(d.getCharts(), chart.getId());
					}
				}
			},

			modify: function(dashboardId, chartId, chartData, cb) {
				var d = _CMCache.getDashboardById(dashboardId);
				if (d) {
					var chart = CMDBuild.model.CMDashboardChart.build(chartData);
					d.replaceChart(chartId, chart);
					if (typeof cb == "function") {
						cb(d.getCharts(), chart.getId());
					}
				}
			},

			remove: function(dashboardId, chartId, cb) {
				var d = _CMCache.getDashboardById(dashboardId);
				if (d) {
					d.removeChart(chartId);
					if (typeof cb == "function") {
						cb(d.getCharts());
					}
				}
			}
		}
	};

})();
