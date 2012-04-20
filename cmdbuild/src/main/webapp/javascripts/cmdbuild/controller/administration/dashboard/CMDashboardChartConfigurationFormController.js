(function() {

	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy", {
		dataSourceName: null,
		fillFieldsForChart: Ext.emptyFn,
		getChartData: Ext.emptyFn,
		showChartFields: Ext.emptyFn,
		extractInterestedValues: Ext.emptyFn,
		setChartDataSourceName: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationFormController", {

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormDelegate"
		},

		constructor : function(view) {
			this.callParent(arguments);
			this.view = view;
			this.view.setDelegate(this);
			this.chartTypeStrategy = null;
			this. chartDataSourceName = null;
		},

		initComponent : function() {
			this.callParent(arguments);
		},

		initView: function() {
			this.view.cleanFields();
			this.view.disableFields();
			this.view.hideOutputFields();
		},

		prepareForAdd: function() {
			this.view.cleanFields();
			this.view.hideOutputFields();
			this.view.enableFields();
		},

		prepareForChart: function(chart) {
			this.initView();
			this.view.fillFieldsWith({
				name: chart.getName(),
				description: chart.getDescription(),
				active: chart.isActive(),
				autoLoad: chart.isAutoload(),
				dataSource: chart.getDataSourceName(),
				type: chart.getType()
			});

			this.view.fillDataSourcePanel(chart.getDataSourceInputConfiguration());

			if (this.chartTypeStrategy) {
				this.chartTypeStrategy.showChartFields(chart);
				this.chartTypeStrategy.fillFieldsForChart(chart);
			}
		},

		prepareForModify: function() {
			this.view.enableFields(onlyMutable=true);
		},

		setChartTypeStrategy: function(s) {
			// set also if s is null but check the interface only if is
			// an object. It can be null when add a new chart
			this.chartTypeStrategy = s;

			if (s) {
				CMDBuild.validateInterface(s, "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy");
				this.chartTypeStrategy.setChartDataSourceName(this. chartDataSourceName);
				this.chartTypeStrategy.showChartFields();
			} else {
				this.view.hideOutputFields();
			}
		},

		getFormData: function() {
			var data = this.view.getFieldsValue();
			var chartSpecificData = {};

			if (this.chartTypeStrategy) {
				chartSpecificData = this.chartTypeStrategy.extractInterestedValues(data);
			}

			var out = Ext.apply(extractGeneralData(data), chartSpecificData);
			out.dataSource = this.view.getDataSourceConfiguration();

			return out;
		},

		// viewDelegate

		onTypeChanged: function(type) {
			this.setChartTypeStrategy(getStrategyFor(type, this));
		},

		onDataSourceChanged: function(dsName) {
			var input = _CMCache.getDataSourceInput(dsName);
			this. chartDataSourceName = dsName;
			this.view.showDataSourceInputFields(input);
		},

		onDataSourceInputFieldTypeChanged: function(value, fieldset) {
			var callbacks = {
				free: function(fieldset) {
					fieldset.addTextFieldForDefault();
				},
				classes: function(fieldset) {
					fieldset.addClassesFieldForDefault();
				},
				lookup: function(fieldset) {
					fieldset.addLookupTypesField();
				},
				user: function(fieldset) {
					fieldset.resetFieldset();
				},
				group: function(fieldset) {
					fieldset.resetFieldset();
				}
			}

			callbacks[value](fieldset);
		}
	});

	function extractGeneralData(data) {
		return {
			name: data.name,
			description: data.description,
			active: data.active,
			autoLoad: data.autoLoad,
			type: data.type
		};
	}

	function getStrategyFor(type, me) {
		var strategis = {
			"gauge": CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy
		}

		if (typeof strategis[type] == "function") {
			return new strategis[type](me.view);
		} else {
			return null;
		}
	}
})();