(function() {

	var tr = CMDBuild.Translation.administration.modDashboard.charts;

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController", {

		alias: "controller.cmdashboardchartconfiguration",

		statics: {
			cmcreate: function(view) {
				var s = buildSubControllers(view);
				return new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController(view, s.formController, s.gridController);
			}
		},

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelDelegate",
			gridControllerDelegate: "CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridControllerDelegate"
		},

		constructor : function(view, formController, gridController, proxy) {
			this.callParent(arguments);

			this.dashboard = null;
			this.chart = null;
			this.view = view;
			this.formController = formController;
			this.gridController = gridController;
			this.proxy = proxy || CMDBuild.ServiceProxy.Dashboard.chart;

			this.view.setDelegate(this);
			this.gridController.setDelegate(this);
		},

		initComponent : function() {
			this.callParent(arguments);
		},

		dashboardWasSelected: function(d) {
			this.dashboard = d;
			this.view.enableTBarButtons(onlyAdd=true);
			this.view.disableButtons();
	
			this.formController.initView(d);
			this.gridController.loadCharts(d.getCharts());
		},

		prepareForAdd: function() {
			// alert("prepareForAdddashboard");
		},

		// viewDelegate
		onModifyButtonClick: function() {
			this.view.disableTBarButtons();
			this.view.enableButtons();
			this.formController.prepareForModify();
		},

		onAddButtonClick: function() {
			this.chart = null;
			this.formController.prepareForAdd();
			this.gridController.clearSelection();
			this.view.disableTBarButtons();
			this.view.enableButtons();
		},

		onPreviewButtonClick: function() {
			var formData = this.formController.getFormData();
			new Ext.window.Window({
				title: formData.name,
				width: 400,
				height: 400,
				layout : 'fit',
				items : getChartFromConfiguration(formData)
			}).show();
		},

		onRemoveButtonClick: function() {
			this.view.disableButtons();
			this.view.disableTBarButtons();
			this.formController.initView();

			var me = this;
			this.proxy.remove(this.dashboard.getId(), this.chart.getId(), function(charts) {
				me.gridController.loadCharts(charts);
			});
		},

		onSaveButtonClick: function() {
			if (!this.formController.isValid()) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			var formData = this.formController.getFormData(),
				me = this,
				cb =  function(charts, idToSelect) {
					me.gridController.loadCharts(charts, idToSelect);
				};

			this.view.disableButtons();
			this.view.disableTBarButtons();
			this.formController.initView();

			if (this.chart) {
				formData.id = this.chart.getId(),
				this.proxy.modify(this.dashboard.getId(), formData.id, formData, cb);
			} else {
				this.proxy.add(this.dashboard.getId(), formData, cb);
			}
		},

		onAbortButtonClick:function() {
			var enableOnlyAddButton = false;
			if (this.chart) {
				this.formController.prepareForChart(this.chart);
			} else {
				this.formController.initView();
				enableOnlyAddButton = true;
			}
			this.view.disableButtons();
			this.view.enableTBarButtons(enableOnlyAddButton);
		},

		// grid controller delegate
		chartWasSelected: function(chart) {
			this.chart = chart;
			this.formController.prepareForChart(chart);
			this.view.disableButtons();
			this.view.enableTBarButtons();
		}
	});

	// TODO: temporary ugly solution
	// Do a preview controller that is able to load the charts

	function getChartFromConfiguration(data) {

		if (!data.type) {
			return {
				xtype: "panel",
				html: tr.alert.wrongConfiguration
			};
		}

		if (data.type == "gauge") {

			var bgcolor = data.bgcolor || '#ffffff';
			var fgcolor = data.fgcolor || '#99CC00';
	
			return {
				xtype : 'chart',
				animate : {
					easing : 'elasticIn',
					duration : 2000
				},
				store : Ext.create('Ext.data.JsonStore', {
					fields : ['name', 'data1', 'data2', 'data3', 'data4', 'data5', 'data6', 'data7', 'data9', 'data9'],
					data : generateData(5)
				}),
				insetPadding : 25,
				flex : 1,
				axes : [{
					type : 'gauge',
					position : 'gauge',
					minimum : data.minimum || 0,
					maximum : data.maximum || 1000,
					steps : data.steps || 20,
					margin: 5
				}],
				series : [{
					type : 'gauge',
					field : 'data1',
					donut : 60,
					colorSet : [fgcolor, bgcolor]
				}]
			};
		}

		if (data.type == "pie") {
			return {
				xtype : 'chart',
				legend: data.legend,
				animate : {
					easing : 'elasticIn',
					duration : 2000
				},
				store : Ext.create('Ext.data.JsonStore', {
					fields : ['name', 'data1', 'data2', 'data3', 'data4', 'data5', 'data6', 'data7', 'data9', 'data9'],
					data : generateData(5)
				}),
				series : [ {
					type : 'pie',
					field : 'data1',
					showInLegend : true,
					highlight : {
						segment : {
							margin : 5
						}
					},
					label : {
						field : 'name',
						display : 'rotate',
						contrast : true,
						font : '1.3em Arial'
					}
				} ]
			};
		}
	}

	function buildSubControllers(view) {
		return {
			formController: CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationFormController.cmcreate(view.getFormPanel()),
			gridController: new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridController.cmcreate(view.getGridPanel())
		};
	}


// TODO do something better

	generateData = function(n, floor) {
		var data = [], p = (Math.random() * 11) + 1, i;
		floor = (!floor && floor !== 0) ? 20 : floor;

		for( i = 0; i < (n || 12); i++) {
			data.push({
				name : Ext.Date.monthNames[i % 12],
				data1 : Math.floor(Math.max((Math.random() * 100), floor)),
				data2 : Math.floor(Math.max((Math.random() * 100), floor)),
				data3 : Math.floor(Math.max((Math.random() * 100), floor)),
				data4 : Math.floor(Math.max((Math.random() * 100), floor)),
				data5 : Math.floor(Math.max((Math.random() * 100), floor)),
				data6 : Math.floor(Math.max((Math.random() * 100), floor)),
				data7 : Math.floor(Math.max((Math.random() * 100), floor)),
				data8 : Math.floor(Math.max((Math.random() * 100), floor)),
				data9 : Math.floor(Math.max((Math.random() * 100), floor))
			});
		}
		return data;
	};

	generateDataNegative = function(n, floor) {
		var data = [], p = (Math.random() * 11) + 1, i;
		floor = (!floor && floor !== 0) ? 20 : floor;

		for( i = 0; i < (n || 12); i++) {
			data.push({
				name : Ext.Date.monthNames[i % 12],
				data1 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data2 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data3 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data4 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data5 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data6 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data7 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data8 : Math.floor(((Math.random() - 0.5) * 100), floor),
				data9 : Math.floor(((Math.random() - 0.5) * 100), floor)
			});
		}
		return data;
	};
})();
