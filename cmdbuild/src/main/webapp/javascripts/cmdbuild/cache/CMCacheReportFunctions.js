(function() {

	var reports = {};

	Ext.define("CMDBUild.cache.CMCacheReportFunctions", {
		addReports: function(rr) {
			for (var i=0, l=rr.length; i<l; ++i) {
				this.addReport(rr[i]);
			}
		},

		addReport: function(r) {
			var report = Ext.create("CMDBuild.cache.CMReportModel", r);
			reports[r.id] = report;

			return report;
		},

		getReports: function() {
			return reports;
		},

		getReportsById: function(id) {
			return reports[id] || null;
		}
	});
})();