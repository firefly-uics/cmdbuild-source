(function() {

	Ext.define("CMDBuild.view.administraton.accordion.CMClassAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Class",
		cmName: "class",
		buildTreeStructure: function() {
			var classes = _CMCache.getClasses();
			var standard = []; // the standard CMDBuild classes
			var simpletables = []; // the tables that does not inherit from Class (the root of all evil)
			var nodesMap = {};

			for (var key in classes) {
				var nodeConf =  buildNodeConf(classes[key]);
				nodesMap[nodeConf.id] = nodeConf;
			}

			for (var id in nodesMap) {
				var node = nodesMap[id];
				if (node.tableType == "standard") {
					if (node.parent) {
						linkToParent(node, nodesMap)
					} else {
						standard.push(node);
					}
				} else {
					simpletables.push(node);
				}
			}

			if (simpletables.length == 0) {
				return standard;
			} else {
				return buildFakeRoot(standard, simpletables);
			}
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
			text: node.get("text") != "Class" ? node.get("text") : "@@Standard",
			tableType: node.get("tableType"),
			leaf: true,
			cmName: node.get("text") != "Class" ? "class" : "",
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