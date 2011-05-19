(function() {
	var tr = CMDBuild.Translation;
	var st = CMDBuild.Constants.cachedTableType.simpletable;
	var cl = CMDBuild.Constants.cachedTableType["class"];
	
	function onNewClass(table) {
		var node = CMDBuild.TreeUtility.buildNodeFromTable(table);
		appendNodeToRightParent(node, table.parent, this);
		this.treePanel.selectNodeById(node.id);
	};
	
	function onNewSimple(table) {
		var node = CMDBuild.TreeUtility.buildNodeFromTable(table);
		appendNodeToRightParent(node, st, this);
	};
	
	function appendNodeToRightParent(node, parentId, tree) {
		var parent = tree.treePanel.searchNodeById(parentId);
		if (parent) {
			parent.appendChild(node);
		} else {
			var tr = CMDBuild.Translation.common.tree_names;
			var newSimpleTableTree = CMDBuild.Cache.getTree(st, rootId=st, rootText=tr.simpletable);
			var newClassTableTree = CMDBuild.Cache.getTree(cl, rootId=undefined, rootText=tr["class"]);
			var root = tree.treePanel.manageMultipleRoots([newClassTableTree, newSimpleTableTree]);
			tree.treePanel.setRootNode(root);
		}
	};
	
	CMDBuild.ClassTreePanelController = Ext.extend(CMDBuild.TreePanelController, {
		deselectOn: "cmdb-addclassAction",
		initComponent : function() {
			CMDBuild.ClassTreePanelController.superclass.initComponent.apply(this, arguments);
			this.listen("cmdb-new-"+cl, onNewClass);
			this.listen("cmdb-new-"+st, onNewSimple);
			
			this.listen("cmdb-deleted-"+cl, this.onDeletedNode);
			this.listen("cmdb-deleted-"+st, this.onDeletedNode);
			
			this.listen("cmdb-modify-"+cl, this.onModifyNode);
			this.listen("cmdb-modify-"+st, this.onModifyNode);
			
			this.listen("cmdb-update-geoattr", function(id) {
				var node = this.treePanel.searchNodeById(id);
				if (node) {
					this.onSelectNode(node);
				}
			}, this);
			
		}
	});
})();