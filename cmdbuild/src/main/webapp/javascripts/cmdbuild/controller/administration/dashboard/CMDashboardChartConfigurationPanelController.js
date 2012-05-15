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
			var formData = CMDBuild.model.CMDashboardChart.build(this.formController.getFormData());
			var store = CMDBuild.view.management.dashboard.CMChartPortletController.buildStoreForChart(formData);

			var chartWindow = new CMDBuild.view.management.dashboard.CMChartWindow({
				chartConfiguration: formData,
				store: store,
				title: formData.name,
				width: 400,
				height: 400
			}).show();

			if (chartWindow.chartPortlet) {
				new CMDBuild.view.management.dashboard.CMChartPortletController(chartWindow.chartPortlet, formData, store);
			}
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

	function buildSubControllers(view) {
		return {
			formController: CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationFormController.cmcreate(view.getFormPanel()),
			gridController: new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridController.cmcreate(view.getGridPanel())
		};
	}

})();