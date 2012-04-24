(function() {
	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartPieStrategy", {
		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy",

		interestedFields: ['singleSerieField', 'labelField', 'legend'],

		// override
		fillFieldsForChart: function(chart) {
			this.form.fillFieldsWith({
				labelField: chart.getLabelField(),
				singleSerieField: chart.getSingleSerieField(),
				legend: chart.withLegend()
			});
		},

		// override
		updateDataSourceDependantFields: function() {
			this.form.setSingleSerieFieldAvailableData(this.getAvailableDsOutputFields(["integer"]));
			this.form.setLabelFieldAvailableData(this.getAvailableDsOutputFields());
		}
	});
})();