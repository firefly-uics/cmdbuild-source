(function() {
	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy", {
		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy",

		constructor: function(form) {
			this.form = form;
		},

		interestedFields: ['maximum', 'minimum', 'steps', 'fgcolor', 'bgcolor', 'singleSerieField'],

		fillFieldsForChart: function(chart) {
			this.form.fillFieldsWith({
				maximum: chart.getMaximum(),
				minimum: chart.getMinimum(),
				fgcolor: chart.getFgColor(),
				bgcolor: chart.getBgColor(),
				steps: chart.getSteps(),
				singleSerieField: chart.getSingleSerieField()
			});
		},

		extractInterestedValues: function(data) {
			var out = {},
				me = this;

			for (var i=0, l=me.interestedFields.length; i<l; ++i) {
				out[me.interestedFields[i]] = data[me.interestedFields[i]];
			}

			return out;
		},

		showChartFields: function() {
			this.form.showFieldsWithName(this.interestedFields);
			this.form.setSingleSerieFieldAvailableData(getAvailableDsOutputFields(this.dataSourceName));
		},

		setChartDataSourceName: function(dsName) {
			this.dataSourceName = dsName;
		},

		// private
		getAvailableDsOutputFields: getAvailableDsOutputFields
	});

	function getAvailableDsOutputFields(dsName) {
		var dataSourceOutput = [];
		if (dsName) {
			dataSourceOutput = _CMCache.getDataSourceOutput(dsName);
		}

		var out = [];
		for (var i=0, l=dataSourceOutput.length, d; i<l; ++i) {
			d = dataSourceOutput[i];
			if (d.type == "integer") {
				out.push([d.name]);
			}
		}

		return out;
	}
})();