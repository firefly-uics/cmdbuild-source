(function() {

	Ext.define("CMDBuild.view.administration.tasks.CMTasksAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Tasks",
		cmName: "tasks",
		buildTreeStructure: function() {
			var nodes = [];
			nodes.push(buildNodeConf({ id: "1", text: "@@ All", type:"all"}));
			nodes.push(buildNodeConf({ id: "2", text: "@@ Mails", type:"mail"}));
			return nodes;
		}
	});
	
	function buildNodeConf(r) {
		return {
			id: r.id,
			text: r.text,
			leaf: true,
			cmName: "tasks",
			type: r.type
		};
	}

})();