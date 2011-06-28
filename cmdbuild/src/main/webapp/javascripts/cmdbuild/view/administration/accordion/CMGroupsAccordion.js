(function() {

	Ext.define("CMDBuild.view.administraton.accordion.CMGroupsAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Groups",
		cmName: "group",
		buildTreeStructure: function() {
			var groups = _CMCache.getGroups();
			var nodes = [];

			for (var key in groups) {
				nodes.push(buildNodeConf(groups[key]));
			}

			return [{
				id: "@@ groups",
				text: "@@ groups",
				leaf: false,
				cmName: "group",
				children: nodes
			}, {
				text: "@@ users",
				cmName: "users",
				leaf: true
			}];

		}
	});
	
	function buildNodeConf(g) {
		return {
			id: g.get("id"),
			text: g.get("text"),
			leaf: true,
			cmName: "group"
		};
	}

})();