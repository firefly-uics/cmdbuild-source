(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Tasks",
		cmName: "tasks",
		buildTreeStructure: function() {
			var nodes = [];
			nodes.push(buildNodeConf({ id: "1", text: "@@ All"}));
			nodes.push(buildNodeConf({ id: "2", text: "@@ Mails"}));
			return nodes;
		}
	});
	
	function buildNodeConf(r) {
		return {
			id: r.id,
			text: r.text,
			leaf: true,
			cmName: "tasks",
		};
	}

})();