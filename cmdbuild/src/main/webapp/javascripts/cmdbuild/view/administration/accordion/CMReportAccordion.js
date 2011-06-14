(function() {

	Ext.define("CMDBuild.view.administraton.accordion.CMReportAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Report",
		cmName: "report",
		buildTreeStructure: function() {
			var reports = _CMCache.getReports();
			var nodes = [];

			for (var key in reports) {
				nodes.push(buildNodeConf(reports[key]));
			}

			return nodes;

		}
	});
	
	function buildNodeConf(r) {
		return {
			id: r.get("id"),
			text: r.get("text"),
			leaf: true,
			cmName: "report",
			group: r.get("group"),
			type: r.get("type")
		};
	}

})();