(function() {
	function onDeletedNode(parameters) {
		var node = this.treePanel.searchNodeById(parameters.id);
		if (node) {
			try {
				node.remove();
			} catch (e) {
				renderNodePathToAllowTheRemove(node, this.treePanel);
				node.remove();
			}
		}
	};
	
	function onModifyNode(parameters) {
		var oldNodeId = parameters.oldId || parameters.id;
		var oldNode = this.treePanel.searchNodeById(oldNodeId);
		
		if (oldNode && oldNode.parentNode) { 
			var newLType = CMDBuild.Cache.getTableById(parameters.id);
			var newNode = CMDBuild.TreeUtility.buildNodeFromTable(newLType);
			while (oldNode.firstChild) {
				var c = oldNode.removeChild(oldNode.firstChild, false);
				newNode.appendChild(c);
			}
			oldNode.parentNode.replaceChild(newNode, oldNode);
			
			if (this.silentListener) {
				this.treePanel.silentSelectNodeById(this.silentListener);
			} else {
				this.treePanel.selectNodeById(newNode.id, true);
			}
		}
	};
	
	function onNewNode(parameters) {
		if (parameters) {
			var node = CMDBuild.TreeUtility.buildNodeFromTable(parameters);
			var parent = this.treePanel.appendNewNode(node, parameters.parent);
			if (!this.silentListener) {
				this.treePanel.selectNodeById(node.id);
			}
		}
	};
	
	function renderNodePathToAllowTheRemove(node, tree) {
		tree.expandPath(node.getPath());
	};

	function deselect() {
		this.treePanel.deselect();
	};
	
	CMDBuild.TreePanelController = Ext.extend(Ext.Component, {
		eventType: undefined,
		treePanel: undefined,
		silentListener: false, // a silent listener listen the change but doesn't show the tree after that
		initComponent : function() {
			CMDBuild.TreePanelController.superclass.initComponent.apply(this, arguments);
			if (this.treePanel) {
				this.treePanel.subscribeListener(this);				
			} else {
				throw new Error("CMDBuild.TreePanelController must be istantiated with a Ext.tree.TreePanel");
			}
			
			if (this.eventType) {
				var et = this.eventType;
				this.subscribe("cmdb-deleted-"+et, onDeletedNode, this);
				this.subscribe("cmdb-modify-"+et, onModifyNode, this);
				this.subscribe("cmdb-new-"+et, this.onNewNode, this);
			}
			
			if (typeof this.deselectOn == "string") {
				this.listen(this.deselectOn, deselect);
			} else if (Ext.isArray(this.deselectOn)) {
				for (var i=0, l=this.deselectOn.length; i<l; ++i) {
					this.listen(this.deselectOn[i], deselect);
				}
			}
		},
		onSelectNode: function(node) {
			if (node) {
				var attributes = CMDBuild.Cache.getTableById(node.id) || node.attributes;
				attributes.tabToOpen = CMDBuild.Constants.tabNames.card;
				if (attributes && attributes.selectable) {
					this.fireEvent("selectionchange", {
						selection: attributes,
						controllerId: this.id
					});
					this.publish("cmdb-select-"+attributes.type, attributes);
				}
			}
		},
		listen: function(event, fn) {
			this.subscribe(event, fn, this);
		},
		openCard: function(p, silent) {
			var table = p.table;
			var node = this.treePanel.searchNodeById(p.table.id);
			if (node) {
				this.treePanel.silentSelectNodeById(node.id, expandAfter=!silent);
				if (!silent) {
					var params = Ext.apply({}, p.table);
					params.cardId = p.cardId;
					params.tabToOpen = p.tabToOpen;
					this.publish("cmdb-select-"+params.type, params);
				}
				return true;
			}
			return false;
		},
		onNewNode: onNewNode,
		onModifyNode: onModifyNode,
		onDeletedNode: onDeletedNode
	});
})();