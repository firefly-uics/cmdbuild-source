(function() {

	var reports = {};
	var gridStore = null;
	var comboStore = null;

	Ext.define("CMDBUild.cache.CMCacheReportFunctions", {
		addReports: function(rr) {
			for (var i=0, l=rr.length; i<l; ++i) {
				this.addReport(rr[i]);
			}
		},

		addReport: function(r) {
			var report = Ext.create("CMDBuild.model.report.Cache", r);
			reports[r.id] = report;

			return report;
		},

		reloadReportStores: function() {
			if (comboStore) {
				comboStore.load();
			}
		},

		getReports: function() {
			return reports;
		},

		getReportById: function(id) {
			return reports[id] || null;
		},

		getReportComboStore: function() {
			if (comboStore == null) {
				comboStore = new Ext.data.Store({
					model: "CMDBuild.model.CMReportAsComboItem",
					proxy: {
						type: "ajax",
						url: 'services/json/management/modreport/getreportsbytype',
						reader: {
							type: "json",
							root: "rows",
							totalProperty: 'results'
						},
						extraParams: {
							type: "custom",
							limit: 1000
						}
					},
					autoLoad: true
				})
			}

			return comboStore;
		}
	});

})();