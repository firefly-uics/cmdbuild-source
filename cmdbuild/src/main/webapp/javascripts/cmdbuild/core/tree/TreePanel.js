(function() {
var _suspendNextExpandEvent = false;

var selectNode = function(tree, node, expandAfter, silent) {
	if (node) {
		// put the instruction at the end of the event queue
		// to wait the rendering of the bloody Ext.tree.TreeNode
		(function() {
			if (silent) {
				tree.getSelectionModel().suspendEvents(false);
			}
			tree.selectPath(node.getPath());
			if (expandAfter) {
				_suspendNextExpandEvent = silent;
				tree.expand();
			}
			if (silent) {
				tree.getSelectionModel().resumeEvents();
			}
		}).defer(1, tree);
		return true;
	}
	return false;
};

var silentSelectNode = function(tree, node, expandAfter) {
	return selectNode(tree, node, expandAfter, true);
};

var manageMultipleRoots = function(roots) {
	var candidates = [];
	var root;
	for (var i=0, l=roots.length; i<l; ++i) {
		var root = roots[i];
		if (root && root.childNodes.length > 0) {
			candidates.push(roots[i]);
		}
	}
	
	if (candidates.length == 1) {
		root = candidates[0];
	} else {
		root = new Ext.tree.TreeNode();
		root.appendChild(candidates);
	}
	
	return root;
};

CMDBuild.TreePanel = Ext.extend(Ext.tree.TreePanel, {
	collapsed: true,
	fakeNodeEventName: undefined,
	autoScroll: true,
	initComponent: function() {
		var roots = this.root;
		
		if (Ext.isArray(roots)) {
			this.root = manageMultipleRoots(roots);			
		}
		if (!this.root) {
			this.root = new Ext.tree.TreeNode();;
			this.rootVisible = false;
		}
		
		if (!this.rootVisible) {
			this.root.attributes.selectable = false;
		}
		
		CMDBuild.TreePanel.superclass.initComponent.apply(this, arguments);
		// listener interface
		this.listener = {
			onSelectNode: function(node) {
				throw new Error("This method must be implemented to a real listener");
			}
		}; 
		
		this.getSelectionModel().on("selectionchange", function(sm, node) {        	
			this.listener.onSelectNode(node);      	
        }, this);
		
		this.on("expand", function() {
			if (_suspendNextExpandEvent) {
				_suspendNextExpandEvent = false;
				return;
			}
			var node = this.getSelectionModel().getSelectedNode();
			if (node) {
				this.listener.onSelectNode(node);
			} else {
				var selected = this.selectFirstSelectableNode();
				if (!selected && this.fakeNodeEventName) {
					var fakeNode = new Ext.tree.TreeNode({
						id: "fakeNode",
						type: this.fakeNodeEventName,
						selectable: true
					});
					this.listener.onSelectNode(fakeNode);
				}
			}
		}, this);
	},
	
	subscribeListener: function(component) {
		this.listener = component;
	},
	
	selectFirstSelectableNode: function(expandAfter) {
		var node = CMDBuild.TreeUtility.findFirsSelectableNode(this.root);
		return selectNode(this, node, expandAfter);
	},
	
	selectNodeById: function(id, expandAfter) {
		var node = this.searchNodeById(id);
		return selectNode(this, node, expandAfter);
	},
	
	silentSelectNodeById: function(id, expandAfter) {
		var node = this.searchNodeById(id);
		return silentSelectNode(this, node, expandAfter);	
	},
	
	searchNodeById: function(id) {
		return CMDBuild.TreeUtility.searchNodeByAttribute({
			attribute: "id",
			value: id,
			root: this.root
		});
	},
	
	appendNewNode: function(node, parentId) {
		var parent = this.searchNodeById(parentId) || this.getRootNode();
		parent.appendChild(node);
	},
	
	deselect: function() {
		this.getSelectionModel().clearSelections();
	},
	
	manageMultipleRoots: manageMultipleRoots
});
})();