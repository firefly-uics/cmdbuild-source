(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMDashboardAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modDashboard.title,

		cmName: "dashboard",

		buildTreeStructure: function() {
			var domains = _CMCache.getDashboards();
			var out = [];

			for (var key in domains) {
				out.push(buildNodeConf(domains[key]));
			}

			return out;
		}
	});

	function buildNodeConf(d) {
		return {
			id: d.get("id"),
			text: d.get("description"),
			leaf: true,
			cmName: "dashboard",
			iconCls: "dashboard"
		};
	}

})();