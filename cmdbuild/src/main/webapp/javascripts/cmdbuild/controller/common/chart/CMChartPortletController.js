(function() {
	Ext.define("CMDBuild.view.management.dashboard.CMChartPortletController", {
		statics: {
			buildStoreForChart: function(chartConfiguration) {
				var fields = [];
				if (chartConfiguration.getSingleSeriesField()) {
					fields = [chartConfiguration.getSingleSeriesField()];
					if (chartConfiguration.getLabelField()) {
						fields = fields.concat(chartConfiguration.getLabelField());
					}
				} else {
					fields = [chartConfiguration.getCategoryAxisField()].concat(chartConfiguration.getValueAxisFields());
				}
	
				return Ext.create('Ext.data.JsonStore', {
					fields : fields,
					data : []
				});
			}
		},

		constructor: function(view, chartConfiguration, store) {
			this.view = view;
			this.chartConfiguration = chartConfiguration;
			this.store = store;

			this.view.setDelegate(this);

			if (chartConfiguration.isAutoload()) {
				this.onFormLoadButtonClick();
			} else {
				this.view.showParamsForm(toggle=true);
			}
		},

		// as view delegate
		onReloadButtonClick: function() {
			var loaded = false;
			if (this.view.formIsValid()) {
				this.store.loadData(generateData(this.chartConfiguration.getDataSourceName()));
				loaded = true;
			} else {
				this.view.showParamsForm(toggle=true);
			}

			return loaded;
		},

		onFormLoadButtonClick: function(panel) {
			var loaded = this.onReloadButtonClick();

			if (!this.view.chartRendered && loaded) {
				this.view.renderChart();
			}

			if (this.chartConfiguration.getDataSourceInputConfiguration().length == 0) {
				// The user has clicked the load button in chart without input parameters
				// so there are no reasons to leave the panel showed
				this.view.hideParamsForm(toggle=true);
			}
		}
	});

	// Super fake to allow the development
	// as soon replaced with real server calls
	function generateData (sourceName) {
		var sources = {
			cm_card_per_asset_subclass: function() {
				var data = [];
				var assetsSubclasses = ["Notebook", "PC", "Server", "License",
				"Monitor", "Network Device", "Printer", "Rack", "UPS"];

				for (var i=0, l=assetsSubclasses.length; i<l; ++i) {
					var rec = {
						nome_classe: assetsSubclasses[i],
						numero_card: Math.floor(Math.max((Math.random() * 100), 20))
					};

					data.push(rec);
				}

				return data;
			},

			gauge_datasource: function() {
				var cardCancellate = Math.floor(Math.max((Math.random() * 200)));

				return [{
					card_aperte: 200 + cardCancellate,
					card_cancellate: cardCancellate
				}];
			},

			cm_brand_asset: function() {
				var data = [];
				var brands = ["IBM", "HP", "Sony", "Cisco", "Acer", "Canon", "Epson", "Microsoft"];
				for (var i=0, l=brands.length; i<l; ++i) {
					var rec = {
						brand_name: brands[i],
						count: Math.floor(Math.max((Math.random() * 100), 20))
					};

					data.push(rec);
				}

				return data;
			},

			cm_nagios_allarms: function() {
				var data = [];
				var months = [
					"Gennaio", "Febbraio", "Marzo", "Aprile",
					"Maggio", "Giugno", "Luglio", "Agosto",
					"Settebre", "Ottobre", "Novembre", "Dicembre"
				];

				for (var i=0, l=months.length; i<l; ++i) {
					var rec = {
						mese: months[i],
						count_2011: Math.floor(Math.max((Math.random() * 100), 20)),
						count_2012: Math.floor(Math.max((Math.random() * 100), 20))
					};

					data.push(rec);
				}

				return data;
			}
		};

		if (typeof sources[sourceName] == "function") {
			return sources[sourceName]();
		} else {
			return [];
		}

	};
})();