(function() {

	var pkg = function(s) {
		return "CMDBuild.view.administration.dashboard." + s;
	};

	Ext.define(pkg("CMDashboardChartConfigurationGridDelegate"), {
		onChartSelect: Ext.emptyFn
	});

	var tr = CMDBuild.Translation.administration.modDashboard.charts;

	Ext.define(pkg("CMDashboardChartConfigurationGrid"), {
		extend : "Ext.grid.Panel",

		alias : "widget.dashboardchartsconfigurationgrid",

		constructor : function() {
			this.callParent(arguments);
		},

		initComponent : function() {
			var store = Ext.create('Ext.data.Store', {
				model: "CMDBuild.model.CMDashboardChart",
				proxy : {
					type : 'memory',
					reader : {
						type : 'json',
						root : 'items'
					}
				}
			});

			Ext.apply(this, {
				store: store,
				columns : [{
					header : tr.fields.name,
					dataIndex : 'name',
					flex : 1
				}, {
					header : tr.fields.chartType,
					dataIndex : 'type',
					renderer: function(value) {
						return tr.availableCharts[value]
					},
					flex : 1
				}, {
					header: tr.fields.dataSource,
					dataIndex: 'dataSource',
					renderer: function(value){
						var out = "";
						if (Ext.getClassName(value) == "CMDBuild.model.CMDashboardChartDataSource") {
							out = value.getName() || "";
						}

						return out;
					},
					flex: 1
				}],
			});

			this.callParent(arguments);

			var me = this;
			this.mon(this.getSelectionModel(), "select", function(sm, chart) {
				me.delegate.onChartSelect(chart);
			});
		},

		clearSelection: function() {
			var sm = this.getSelectionModel();
			if (sm) {
				sm.deselectAll();
			}
		},

		selectRecord: function(r) {
			var sm = this.getSelectionModel();
			if (sm && r) {
				sm.select(r);
			}
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, pkg("CMDashboardChartConfigurationGridDelegate"));
			this.delegate = d;
		}
	});
})();