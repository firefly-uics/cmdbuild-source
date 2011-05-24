(function() {

	var tr = CMDBuild.Translation.administration.modcartography;
	
	var store = Ext.create('Ext.data.TreeStore', {
		fields: [
			{name: "cmName", type: "string"},
			{name: "text", type: "string"},
			{name: "id", type: "string"},
			{name: "parent", type: "string"}
		],
		root : {
			expanded : true,
			children : []
		}
	});

	Ext.define("CMDBuild.view.administraton.accordion.CMLookupAccordion", {
		extend: 'Ext.tree.Panel',
		title: "@@ Lookup",
		store: store,
		rootVisible: false,
		hideMode: "offsets",
		cmName: "lookuptype",
		
		updateStore: function() {
			var root = this.store.getRootNode();
			var treeStructure = buildTreeStructure();
			root.removeAll();
			root.appendChild(treeStructure);
			this.store.sort("text", "ASC");
		},
		
		selectNodeById: function(nodeId) {
			var sm = this.getSelectionModel();
			var node = this.store.getNodeById(nodeId);
			if (node) {
				sm.select(node);
				node.bubble(function() {
					this.expand();
				});
			} else {
				_debug("I have not find a node with id " + nodeId);
			}
		}
	});
	
	function buildTreeStructure() {
		var lookupTypes = CMDBuild.Cache.getTablesByGroup(CMDBuild.Constants.cachedTableType.lookuptype);
		var out = [];
		var nodesMap = {};

		for (var key in lookupTypes) {
			var nodeConf =  buildNodeConf(lookupTypes[key]);
			nodesMap[nodeConf.id] = nodeConf;
		}

		for (var id in nodesMap) {
			var node = nodesMap[id];
			if (node.parent) {
				var parentNode = nodesMap[node.parent];
				parentNode.children = (parentNode.children || []);
				parentNode.children.push(node);
				parentNode.leaf = false;
			} else {
				out.push(node);
			}
		}

		function buildNodeConf(lt) {
			return {
				id: lt.id,
				text: lt.text,
				leaf: true,
				cmName: "lookuptype",
				parent: lt.parent
			};
		}

		return out;
	};

})();