(function() {
	CMDBuild.ServiceProxy.url.Dashboard = {
		save : "",
		list : "services/json/dashboard/list",
		remove : ""
	};

	CMDBuild.ServiceProxy.Dashboard = {
		list : function(p) {
			// p.method = "GET";
			// p.url = CMDBuild.ServiceProxy.url.Dashboard.list;

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
							autoLoad: true,
							dataSourceName: 'cm_datasource_1',
							dataSourceParameters: [{
								name: 'in11',
								type: 'integer',
								fieldType: 'free',
								defaultValue: 545
							}],
							minimum: 10,
							maximum: 1000,
							steps: 20,
							singleSeriesField: "out11",
							labelField: "out12"
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
							name: "intero",
							type: "INTEGER"
						},{
							name: "double",
							type: "DOUBLE"
						},{
							name: "decimal",
							type: "DECIMAL"
						},{
							name: "Data",
							type: "DATE"
						},{
							name: "Timestamp",
							type: "TIMESTAMP"
						},{
							name: "Time",
							type: "TIME"
						},{
							name: "String",
							type: "STRING"
						},{
							name: "Char",
							type: "CHAR"
						},,{
							name: "Text",
							type: "TEXT"
						},{
							name: "Inet",
							type: "INET"
						},{
							name: "Boolean",
							type: "BOOLEAN"
						}],

						output: [{
							name: "out11",
							type: "INTEGER"
						},{
							name: "out12",
							type: "STRING"
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
			};

			p.success.call(this, null, null, decoded);
			p.callback.call();


//			/*
//			 *  real call, but withouth dashboards
//			 */
//			p.method = "GET";
//			p.url = CMDBuild.ServiceProxy.url.Dashboard.list;
//
//			var success = p.success;
//
//			p.success = function(response, options, decoded) {
//				decoded.response.dashboards = [{
//					id : 1,
//					groups : [14],
//					name : "Foo",
//					description : "Amazing dashboard for amazing people",
//					charts : []
//				}, {
//					id : 2,
//					groups : [],
//					name : "Bar",
//					description : "Cool dashboard for cool people",
//					charts : []
//				}];
//
//				success(response, options, decoded);
//			};
//
//			CMDBuild.ServiceProxy.core.doRequest(p);
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
			},

			move: function(fromDashboardId, toDashboardId, chartId, cb) {
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
		}
	};

})();
