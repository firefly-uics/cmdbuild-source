(function() {

	Ext.define("CMDBuild.view.administraton.accordion.CMProcessAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Process",
		cmName: "process",

		buildTreeStructure: function() {
			var processes = _CMCache.getProcesses();
			var nodesMap = {};
			var out = [];

			for (var key in processes) {
				var nodeConf =  buildNodeConf(processes[key]);
				nodesMap[nodeConf.id] = nodeConf;
			}

			for (var id in nodesMap) {
				var node = nodesMap[id];
				if (node.parent) {
					linkToParent(node, nodesMap)
				} else {
					out.push(node);
				}
			}
			
			return out;
		},

		afterUpdateStore: function() {
			var root = this.store.getRootNode();
			if (root.childNodes.length == 1) {
				this.store.setRootNode(root.getChildAt(0).remove(destroy=false));
			}
		}
	});

	function buildNodeConf(node) {
		return {
			id: node.get("id"),
			text: node.get("text"),
			tableType: node.get("tableType"),
			leaf: true,
			cmName: "process",
			parent: node.get("parent")
		};
	}

	function linkToParent(node, nodesMap) {
		var parentNode = nodesMap[node.parent];
		parentNode.children = (parentNode.children || []);
		parentNode.children.push(node);
		parentNode.leaf = false;
	}

	function buildFakeRoot(standard, simpletables) {
		return {
			leaf: false,
			children:[
				standard[0],
				{
					text: "@@Simples",
					leaf: false,
					children: simpletables
				}
			]
		}
	}
})();