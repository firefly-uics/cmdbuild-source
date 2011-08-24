(function() {

	Ext.define("CMDBuild.view.administraton.accordion.CMMenuAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.management.modmenu.menu,
		cmName: "menu",
		buildTreeStructure: function(items) {
			var nodesMap = {};
			var out = [];

			for (var i=0, l=items.length; i<l; i++) {
				var nodeConf =  buildNodeConf(items[i]);
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
		}
	});

	function buildNodeConf(node) {
		var type = node.type,
		superclass = node.superclass;

		var n = {
			id: node.id,
			idClass: node.id,
			text: node.text,
			tableType: node.tableType,
			leaf: true,
			cmName: node.type == "processclass" ? "process" : node.type, //ugly compatibility hack
			parent: node.parent,
			iconCls: "cmdbuild-tree-" + (superclass ? "super" : "") + type +"-icon",
			cmIndex: node.cmIndex
		};

		if (isAReport(node)) {
			addReportStuff(n, node)
		}

		return n
	}

	function linkToParent(node, nodesMap) {
		var parentNode = nodesMap[node.parent];
		parentNode.children = (parentNode.children || []);
		parentNode.children.push(node);
		parentNode.leaf = false;
	}

	function isAReport(node) {
		return node.type.indexOf("report") > -1
	}
	
	function addReportStuff(n, node) {
		n.cmName = "report";
		n.objid = node.objid;
		n.subtype = node.type
	}
})();