(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMWFOpenReportControllerWidgetReader",{
		extend: "CMDBuild.controller.management.common.widgets.CMOpenReportControllerWidgetReader",
		getType: function(w) {return w.ReportType},
		getCode: function(w) {return w.ReportCode},
		getPreset: function(w) {return w.parameters},
		getForceFormat: function(w) {return w.forceextension}
	});
})();