(function() {
	var DEFAULT_MENU_TEXT = "@@ Navigation Trees";
	
	Ext.define("CMDBuild.view.administration.navigationTrees.CMNavigationTreesTree",{
//		extend: "CMDBuild.view.administration.classes.CMAttributeForm",
		extend: "Ext.panel.Panel",
		treeName : undefined,
		constructor: function() {

			this.callParent(arguments);
		},
		initComponent : function() {

			this.modifyButton = new Ext.button.Button({
				iconCls : 'modify',
				text: "@@ Modify tree", //this.translation.modify_domain,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onModifyButtonClick");
				}
			});

			this.deleteButton = new Ext.button.Button({
				iconCls : 'delete',
				text: "@@ Remove tree", //this.translation.delete_domain
				scope: this,
				handler: function() {
					this.delegate.cmOn("onDeleteButtonClick");
				}
			});

			this.saveButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.save,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onSaveButtonClick");
				}
			});

			this.abortButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.abort,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onAbortButtonClick");
				}
			});

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];
			this.tree = new CMDBuild.view.administration.navigationTrees.CMTreePanel({
				frame: false
			});
			this.treePanel = new Ext.panel.Panel( {
				region : "center",
				frame : false,
				border : true,
				autoScroll : true,
				width: "100%",
				height: "100%",
				cls: "x-panel-body-default-framed",
				items : [this.tree]
			});


			Ext.apply(this, {
				tbar: this.cmTBar,
				buttonAlign: "center",
				buttons: this.cmButtons,
				frame: false,
				border: false,
				layout: "border",
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmgraypanel',
				items: [this.treePanel]
			});
			
			this.callParent(arguments);
			this.mon(this.tree, "afteritemexpand", function(node) {
				for (var i = 0; i < node.childNodes.length; i++) {
					expandNode(this, node.childNodes[i]);
				}
			}, this);
		},

		onTreeSelected: function(tree) {
			this.disableModify(enableCMTBar = true);
			var entity = _CMCache.getEntryTypeByName(tree.targetClassName);
			this.setTreeForEntryType(entity);
			this.tree.openTreeForTreeType(tree);
		},
		setTreeForEntryType: function(entryType) {
			if (!entryType) {
				this.resetView();
				return;
			}

			this.tree.updateRootForEntryType(entryType);

			return this.tree.getRootNode();
		},

		enableModify: function() {
			this.treePanel.enable();
			this.saveButton.enable();
			this.abortButton.enable();
			this.modifyButton.disable();
			this.deleteButton.disable();
		},

		disableModify: function() {
			this.treePanel.disable();
			this.saveButton.disable();
			this.abortButton.disable();
			this.modifyButton.enable();
			this.deleteButton.enable();
		},

		addDomainsAsFirstLevelChildren: function(domains) {
			return this.tree.addDomainsAsFirstLevelChildren(domains);
		},

		getData: function() {
			return this.tree.getData();
		},
		
		addDomainsAsNodeChildren: function(domains, node) {
			return this.tree.addDomainsAsNodeChildren(domains, node);
		},

	});	
	Ext.define('CMDBuild.model.NavigationTreeNodeModel', {
		extend: 'Ext.data.Model',
		fields: [{
			name: "text",
			type: "string"
		},{
			name: "domain",
			type: "auto"
		},{
			name: "entryType",
			type: "auto"
		},{
			name: "cqlNode",
			type: "text"
		}],

		getDomain: function() {
			return this.get("domain");
		},

		getEntryType: function() {
			return this.get("entryType");
		},

		setEntryType: function(et) {
			this.set("entryType", et);
		},

		setText: function(text) {
			this.set("text", text);
		},

		getNSideIdInManyRelation: function() {
			var d = this.getDomain();
			if (d) {
				return d.getNSideIdInManyRelation();
			}

			return null;
		},

	});

	var NODE_TEXT_TMP = "{0} ({1})";

	var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
		clicksToEdit: 1
	});
	function createRoot() {
		 var root = Ext.create('Ext.data.TreeStore', {
				model: "CMDBuild.model.NavigationTreeNodeModel",
				root: {
					expanded: false,
					checked: true,
					text: DEFAULT_MENU_TEXT,
					children: []
				}
			});
		return root;
	}
	
	Ext.define("CMDBuild.view.administration.navigationTrees.CMTreePanel", {
		extend: "Ext.tree.Panel",
        selModel: {
            selType: 'cellmodel'
        },
        border: false,
        plugins: [cellEditing],
		initComponent: function() {
			this.store = createRoot();

			this.columns = [{
				xtype: 'treecolumn',
				text: "@@ Navigation tree",
				dataIndex: 'text',
				flex: 3,
				sortable: false
			}, {
				dataIndex: 'cqlNode',
				text: "@@ Cql filter",
				flex: 2,
				sortable: false,
				field: {
					allowBlank: true,
					enabled: false
				}
			}];

			this.callParent(arguments);
			this.mon(this, "checkchange", function(node, checked) {
				this.onNavigationTreesItemChecked(node, checked);
			}, this);

		},
		
		onNavigationTreesItemChecked: function(node, checked) {
			this.suspendEvents(false);
			if (checked) {
				checkAllParents(node);
			}
			else {
				unCheckAllChildren(node);
			}
			this.resumeEvents();
		},
		
		addDomainsAsFirstLevelChildren: function(domains) {
			var r = this.getStore().getRootNode();
			this.addDomainsAsNodeChildren(domains, r);
		},
		
		openTreeForTreeType: function(tree) {
			this.suspendEvents(false);
			var thereAreChildren = false;
			var r = this.getStore().getRootNode();
			for (var i = 0; i < tree.childNodes.length; i++) {
				if (this.openNodes(tree.childNodes[i], r))
					thereAreChildren = true;
			}
			if (thereAreChildren) {
				r.expand();
			}
			this.resumeEvents();
		},
		
		addDomainsAsNodeChildren: function(domains, node) {
			node.collapse();
			for (var i=0, l=domains.length; i<l; ++i) {
				var d = domains[i];
				var etId = d.getNSideIdInManyRelation();
				var et = _CMCache.getEntryTypeById(etId);
				node.appendChild({
					text: Ext.String.format(NODE_TEXT_TMP, d.get("description"), et.get("text")),
					checked: false,
					expanded: true,
					domain: d,
					entryType: et,
					children: []
				});
			}
		},

		openNodes: function(nodeSaved, parentComplete) {
			var nodeFound = inChildrenNodes(nodeSaved, parentComplete);
			
			if (nodeFound) {
				expandAllChildrenNodes(this, parentComplete);
				var thereAreChildren = false;
				for (var i = 0; i < nodeSaved.childNodes.length; i++) {
					if(this.openNodes(nodeSaved.childNodes[i], nodeFound)) {
						thereAreChildren = true;
					}
				}
				if (thereAreChildren) {
					nodeFound.expand();
				}
				nodeFound.set("checked", true);
				console.log("check: " + nodeFound.getEntryType() + "  " + Ext.getClassName(nodeFound));
				return true;
			}
			return false;
		},

		updateRootForEntryType: function(entryType) {
			this.getSelectionModel().deselectAll();
			this.store.setRootNode({
				expanded: false,
				checked: true,
				text: entryType.get("text"),
				children: []
			});
			var r = this.store.getRootNode();
			r.setText(entryType.get("text"));
			r.setEntryType(entryType);
			r.removeAll(true);
			r.commit(); // to remove the F____ing red triangle to the node
			var domains = retrieveDomainsForEntryType(entryType, undefined);
			this.addDomainsAsFirstLevelChildren(domains);
		},

		getData: function() {
			var node = this.store.getRootNode();
			return getChildren(node);
		}
		
	});

	function retrieveDomainsForEntryType(entryType, domainName, onlyN_1) {
		var ids =  _CMUtils.getAncestorsId(entryType);
		return _CMCache.getDomainsBy(function(domain) {
			if (! onlyN_1) {
				if (domainName && domain.get("name") == domainName)
					return false;
				return (Ext.Array.contains(ids, domain.getSourceClassId()) || Ext.Array.contains(ids, domain.getDestinationClassId()));
			}
			else {
				var cardinality = domain.get("cardinality");
				if (cardinality == "1:N"
					&& Ext.Array.contains(ids, domain.getSourceClassId())) {
	
					return true;
				}
	
				if (cardinality == "N:1"
					&& Ext.Array.contains(ids, domain.getDestinationClassId())) {
	
					return true;
				}
	
				return false;
			}
		});
	}
	function expandNode(me, node) {
		if (node._alreadyExpanded) {
			return;
		}
		node._alreadyExpanded = true;
		var id = node.getNSideIdInManyRelation();
		if (!id) {
			return;
		}
		var domains = retrieveDomainsForEntryType(id, node.data.domain.get("name"), false);
		me.addDomainsAsNodeChildren(domains, node);
	}
	function expandAllChildrenNodes(me, node) {
		for (var i = 0; i < node.childNodes.length; i++) {
			expandNode(me, node.childNodes[i]);
		}
	}
	function inChildrenNodes(nodeSaved, parentComplete) {
		for (var i = 0; i < parentComplete.childNodes.length; i++) {
			if (isEqual(nodeSaved, parentComplete.childNodes[i])) {
				return parentComplete.childNodes[i];
			}
		}
		return undefined;
	}
	function isEqual(nodeSaved, node) {
		console.log(nodeSaved.domainName + " == " + node.getDomain().get("name") + " && " + nodeSaved.targetClassName + " == " + node.getEntryType());
		return (nodeSaved.domainName == node.getDomain().get("name") && nodeSaved.targetClassName == node.getEntryType());
	}
	function checkAllParents(node) {
		while (node = node.parentNode) {
			node.set("checked", true);
		}
	}
	function unCheckAllChildren(node) {
		for (var i = 0; i < node.childNodes.length; i++) {
			unCheckAllChildren(node.childNodes[i]);
			node.childNodes[i].set("checked", false);
		}
	}
	function getChildren(node) {
		var children = [];
		for (var i = 0; i < node.childNodes.length; i++) {
			var n = node.childNodes[i];
			if (n.get("checked")) {
				children.push(NodeToObject(n));
			}
		}
		return children;
	}
	function NodeToObject(node) {
		var name = node.data.domain.data.nameClass2;
		var et = _CMCache.getEntryTypeByName(name);
		return {
			domainName: node.data.domain.data.name,
			targetClassName: name,
			targetClassDescription: et.get("text"),
			direct: true,
			BaseNode: false,
			childNodes: getChildren(node)
		};
	}

})();