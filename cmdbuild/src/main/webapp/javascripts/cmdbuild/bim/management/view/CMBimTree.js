Ext.define('CMDBuild.bim.management.view.CMBimTreeDelegate', {
	onNodeCheckChange: function(node, check) {},
	onNodeSelect: function(node) {}
});

Ext.define('CMDBuild.bim.management.view.CMBimTree', {
	extend: 'Ext.tree.Panel',
	xtype: 'check-tree',

	rootVisible: false,
	useArrows: true,
	frame: true,

	// configuration
	delegate: undefined,
	// configuration

	initComponent: function() {

		Ext.apply(this, {
			store: new Ext.data.TreeStore(),
			listeners: {
				checkchange: function(node, checked) {
					this.delegate.onNodeCheckChange(node, checked);
				},
				select: function(treePanel, node, index, eOpts) {
					this.delegate.onNodeSelect(node);
				}
			}
		});

		this.delegate = this.delegate
				|| new CMDBuild.bim.management.view.CMBimTreeDelegate();
		this.callParent();
	},

	selectNodeByOid: function(oid) {
		var node = this.findNodeByOid(oid);
		if (node) {
			var me = this;
			this.expandPreviousNodes(node.parentNode,
				Ext.Function.createDelayed(function() {
					var sm = me.getSelectionModel();
					sm.select([node]);
				}, 500)
			);
		}
	},

	expandPreviousNodes: function(node, cb) {
		if (node) {
			var me = this;
			var parent = node.parentNode;

			if (parent) {
				node.expand(false, function() {
					me.expandPreviousNodes(parent, cb);
				});
			} else {
				node.expand(false, cb);
			}
		}
	},

	findNodeByOid: function(oid) {
		var rootNode = this.getRootNode();
		return rootNode.findChildBy(function(aNode) {
			return aNode.raw.oid == oid;
		}, null, true);
	},

	setNodeCheckbox: function(oid, check) {
		var nodeToCheck = this.findNodeByOid(oid);
		if (nodeToCheck) {
			nodeToCheck.set("checked", check);
		}
	},

	checkNode: function(oid) {
		this.setNodeCheckbox(oid, true);
	},

	uncheckNode: function(oid) {
		this.setNodeCheckbox(oid, false);
	}
});