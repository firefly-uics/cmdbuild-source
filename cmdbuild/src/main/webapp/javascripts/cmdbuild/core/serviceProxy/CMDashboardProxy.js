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
						id : 2,
						groups : [],
						name : "Bar",
						description : "Allarmi e statistiche",
						charts : [{
							id: 1,
							name: "openedCard",
							description: "Card aperte",
							type: "gauge",
							active: true,
							autoLoad: false,
							maximum: 1000,
							minimum: 0,
							steps: 10,
							fgcolor: "#99CC00",
							bgcolor: "#dddddd",
							dataSourceName: "gauge_datasource",
							singleSeriesField: "card_aperte",
							height: 300,
							dataSourceParameters: [{
								name: "Classe",
								type: "STRING",
								fieldType: "classes",
								lookupType: undefined
							}]
						}, {
							id: 2,
							name: "deletedCard",
							description: "Card cancellate",
							type: "gauge",
							active: true,
							autoLoad: true,
							maximum: 1000,
							minimum: 0,
							steps: 10,
							fgcolor: "#FF0000",
							bgcolor: "#dddddd",
							height: 300,
							dataSourceName: "gauge_datasource",
							singleSeriesField: "card_cancellate",
							dataSourceParameters: [{
								name: "Classe",
								type: "STRING",
								fieldType: "classes",
								defaultValue: 1655406
							}]
						}, {
							id: 3,
							name: "nagiosAllarm",
							description: "Allarmi Nagios",
							type: "bar",
							active: true,
							autoLoad: false,
							legend: true,
							height: 500,
							dataSourceName: "cm_nagios_allarms",
							categoryAxisField: "mese",
							categoryAxisLabel: "Mesi",
							valueAxisFields: ["count_2011", "count_2012"],
							valueAxisLabel: "Numero Allarmi",
							chartOrientation: "horizontal", // "vertical
							dataSourceParameters: [{
								name: "IP",
								type: "INET",
								defaultValue: "1.1.1.1"
							}, {
								name: "date",
								type: "DATE",
								defaultValue: "12/12/1280"
							}, {
								name: "time",
								type: "TIME",
								defaultValue: "12:12:12"
							}, {
								name: "Timestamp",
								type: "TIMESTAMP"
							}, {
								name: "Classe_string",
								type: "STRING",
								fieldType: "classes",
								defaultValue: "Computer"
							}, {
								name: "Classe_int",
								type: "INTEGER",
								fieldType: "classes",
								defaultValue: 1655406
							}, {
								name: "Pinko",
								type: "INTEGER",
								fieldType: "lookup",
								lookupType: "ccccc"
							}]
						}, {
							id: 4,
							name: "cardPerClassSubclass",
							description: "Card per sottoclasse di asset",
							type: "bar",
							active: true,
							autoLoad: true,
							dataSourceName: "cm_card_per_asset_subclass",
							categoryAxisField: "nome_classe",
							categoryAxisLabel: "Nome classe",
							valueAxisFields: ["numero_card"],
							valueAxisLabel: "Numero di card",
							chartOrientation: "vertical"
						}, {
							id: 5,
							name: "statoDegliAsset",
							description: "Asset per marca",
							type: "pie",
							active: true,
							autoLoad: true,
							legend: true,
							dataSourceName: "cm_brand_asset",
							singleSeriesField: "count",
							labelField: "brand_name"
						}, {
							id: 6,
							name: "nagiosAllarm",
							description: "Allarmi Nagios",
							type: "line",
							active: true,
							autoLoad: false,
							legend: true,
							height: 500,
							dataSourceName: "cm_nagios_allarms",
							categoryAxisField: "mese",
							categoryAxisLabel: "Mesi",
							valueAxisFields: ["count_2011", "count_2012"],
							valueAxisLabel: "Numero Allarmi",
							dataSourceParameters: [{
								name: "Classe_int",
								type: "INTEGER",
								fieldType: "card",
								classToUseForReferenceWidget: "1655741",
								defaultValue: "747"
							}]
						}],

						columns: [{
							width: 0.4,
							charts: [1, 2, 5]
						}, {
							width: 0.6,
							charts: [3, 4, 6]
						}]
					}],

					dataSources: [{
							name: "cm_card_per_asset_subclass",
							input: [],
							output: [{
								name: "nome_classe",
								type: "STRING"
							}, {
								name: "numero_card",
								type: "INTEGER"
							}]
						},{
						name: "cm_nagios_allarms",
						input: [{
							name: "IP",
							type: "INET"
						}, {
							name: "date",
							type: "DATE"
						}, {
							name: "time",
							type: "TIME"
						}, {
							name: "Timestamp",
							type: "TIMESTAMP"
						}, {
							name: "Classe_string",
							type: "STRING"
						}, {
							name: "Classe_int",
							type: "INTEGER"
						}
//						,{
//							name: "double",
//							type: "DOUBLE"
//						},{
//							name: "decimal",
//							type: "DECIMAL"
//						}
//						,{
//							name: "Data",
//							type: "DATE"
//						}
//						,{
//							name: "Timestamp",
//							type: "TIMESTAMP"
//						},{
//							name: "Time",
//							type: "TIME"
//						},{
//							name: "String",
//							type: "STRING"
//						},{
//							name: "Char",
//							type: "CHAR"
//						},,{
//							name: "Text",
//							type: "TEXT"
//						},{
//							name: "Inet",
//							type: "INET"
//						},{
//							name: "Boolean",
//							type: "BOOLEAN"
//						}
						],

						output: [{
							name: "count_2011",
							type: "INTEGER"
						},{
							name: "count_2012",
							type: "INTEGER"
						},{
							name: "mese",
							type: "STRING"
						}]
					}, {
						name: "gauge_datasource",
						input: [{
							name: "Classe",
							type: "STRING"
						}],
						output: [{
							name: "card_aperte",
							type: "INTEGER"
						},{
							name: "card_cancellate",
							type: "INTEGER"
						}]
					}, {
						name: "cm_brand_asset",
						input: [],
						output: [{
							name: "brand_name",
							type: "STRING"
						}, {
							name: "count",
							type: "INTEGER"
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
