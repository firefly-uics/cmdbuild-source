(function() {
	Ext.define("CMDBuild.view.common.CMBaseAccordion.Store", {
		extend: "Ext.data.TreeStore",
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
	})
	
	/*
	 * this class can not be instantiated,
	 * it is a template for the accordions
	 * 
	 * It is a panel with as item a TreePanel,
	 * it may be directly a TreePanel but there are 
	 * problemps with the accordion layout
	 * */
	Ext.define("CMDBuild.view.common.CMBaseAccordion", {
		extend: 'Ext.panel.Panel',
		rootVisible: false,
		animCollapse: false,

		constructor: function() {
			this.store = Ext.create('CMDBuild.view.common.CMBaseAccordion.Store');

			this.tree = Ext.create("Ext.tree.Panel", {
				store: this.store,
				border: false,
				frame: false,
				region: "center",
				bodyStyle: { "border-top": "none" },
				rootVisible: false
			});

			Ext.apply(this, {
				items: [this.tree],
				layout: "border",
				border: false
			});

			this.callParent(arguments);
		},

		updateStore: function(items) {
			var root = this.store.getRootNode();
			var treeStructure = this.buildTreeStructure(items);
			root.removeAll();
			root.appendChild(treeStructure);
			this.store.sort("text", "ASC");
			
			this.afterUpdateStore();
		},

		selectNodeById: function(nodeId) {
			var sm = this.getSelectionModel(),
				node = this.store.getNodeById(nodeId);

			if (node) {
				sm.select(node);
				node.bubble(function() {
					this.expand();
				});
			} else {
				_debug("I have not find a node with id " + nodeId);
			}
		},

		selectNodeByIdSilentry: function(nodeId) {
			this.suspendEvents();
			this.selectNodeById(nodeId);
			this.resumeEvents();
		},

		removeNodeById: function(nodeId) {
			var node = this.store.getNodeById(nodeId);
			if (node) {
				node.remove();
			} else {
				_debug("I have not find a node with id " + nodeId);
			}
		},

		getSelectionModel: function() {
			return this.tree.getSelectionModel();
		},
		
		getNodeById: function(id) {
			return this.store.getNodeById(id);
		},
		
		getRootNode: function() {
			return this.tree.getRootNode();
		},

		getAncestorsAsArray: function(nodeId) {
			var out = [],
				node = this.getNodeById(nodeId);

			if (node) {
				out.push(node);
				while (node.parentNode != null) {
					out.push(node.parentNode);
					node = node.parentNode;
				}
			}
			
			return out;
		},

		buildTreeStructure: function() {
			_debug("CMBaseAccordion.buildTreeStructure Unimplemented method");
		},
		
		afterUpdateStore: function() {
			_debug("CMBaseAccordion.afterUpdateStore Unimplemented method");
		}
	});
})();