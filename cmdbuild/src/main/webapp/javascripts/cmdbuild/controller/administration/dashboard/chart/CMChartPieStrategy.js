(function() {
	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartPieStrategy", {
		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy",

		interestedFields: ['singleSerieField', 'labelField', 'legend'],

		fillFieldsForChart: function(chart) {
			this.form.fillFieldsWith({
				labelField: chart.getLabelField(),
				singleSerieField: chart.getSingleSerieField(),
				legend: chart.withLegend()
			});
		},

		setChartDataSourceName: function(dsName) {
			this.callParent(arguments);
			this.form.setLabelFieldAvailableData(getAvailableDsOutputFields(this.dataSourceName));
		}
	});

	function getAvailableDsOutputFields(dsName) {
		var dataSourceOutput = [];
		if (dsName) {
			dataSourceOutput = _CMCache.getDataSourceOutput(dsName);
		}

		var out = [];
		for (var i=0, l=dataSourceOutput.length, d; i<l; ++i) {
			d = dataSourceOutput[i];
			out.push([d.name]);
		}

		return out;
	}
})();