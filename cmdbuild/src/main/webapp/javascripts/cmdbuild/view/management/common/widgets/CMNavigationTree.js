(function() {
	var DEFAULT_MENU_TEXT = "@@ Navigation Tree";

	Ext.define("CMDBuild.view.management.common.widgets.CMNavigationTree", {
		extend: "Ext.panel.Panel",
		autoScroll: true,	
		statics: {
			WIDGET_NAME: ".NavigationTree"
		},

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.tree = new CMDBuild.view.management.widgets.navigationTree.CMTreePanel();
			this.items = [this.tree];
			this.callParent(arguments);
//			this.tree = new CMDBuild.view.management.widgets.navigationTree.CMTreePanel();
//			this.add(this.tree);
			this.mon(this.tree, "afteritemexpand", function(node) {
				if (node.loaded)
					return;
				else
					node.loaded = true;
				Ext.suspendLayouts();
				expandNodes(this.tree, node.childNodes);
			}, this);
		},

		getExtraButtons: function() {
			var me = this;
			this.saveButton = new Ext.Button( {
				text : CMDBuild.Translation.common.btns.save,
				name : 'saveButton',
				hidden: true,
				handler: function() {
					me.delegate.cmOn("onSave");
				}
			});
			return [this.saveButton];
		},
		
		configureForm: function(treeName, tree) {
			this.tree.loadTree(treeName, tree);
			this.saveButton.show();
		},
		
	});

	Ext.define('CMDBuild.model.widget.NavigationTreeNodeModel', {
		extend: 'Ext.data.Model',
		fields: [{
			name: "text",
			type: "string"
		},{
			name: "cardId",
			type: "string"
		},{
			name: "className",
			type: "string"
		},{
			name: "nodesIn",
			type: "auto"
		},{
			name: "loaded",
			type: "boolean"
		}],

		getNodesIn: function() {
			return this.get("nodesIn");
		},

		setNodesIn: function(nodesIn) {
			this.set("nodesIn", nodesIn);
		},

	});

	function createRoot() {
		 var root = Ext.create('Ext.data.TreeStore', {
				model: "CMDBuild.model.widget.NavigationTreeNodeModel",
				root: {
					expanded: false,
					checked: undefined,
					text: DEFAULT_MENU_TEXT,
					children: []
				}
			});
		return root;
	}
	
	Ext.define("CMDBuild.view.management.widgets.navigationTree.CMTreePanel", {
		extend: "Ext.tree.Panel",
		treeName: undefined,
        selModel: {
            selType: 'cellmodel'
        },
        border: false,
		initComponent: function() {
			this.store = createRoot();

			this.columns = [{
				xtype: 'treecolumn',
				text: "@@ Navigation tree ",
				dataIndex: 'text',
				flex: 3,
				sortable: false
			}];

			this.callParent(arguments);
			this.mon(this, "checkchange", function(node, checked) {
//				this.onNavigationTreesItemChecked(node, checked);
			}, this);

		},
		loadTree: function(treeName, tree) {
			this.getSelectionModel().deselectAll();
			this.store.setRootNode({
				expanded: false,
				checked: undefined,
				text: "@@ Navigation tree " + treeName,
				children: [],
				nodesIn: [tree]
			});
			var r = this.store.getRootNode();
//			r.setText("@@ Navigation tree " + treeName);
			r.removeAll(true);
			r.commit(); // to remove the F____ing red triangle to the node
			loadChildren(r, tree, tree.childNodes);
		}
	});
	function loadChildren(node, tree, nodesIn) {
		CMDBuild.ServiceProxy.getCardList({
			params: {
				className: tree.targetClassName
			},
			success: function(operation, request, decoded) {
				for (var j = 0; j < decoded.rows.length; j++) {
					var row = decoded.rows[j];
					appendNode(node, row.Code + " - " + row.Description, row.Id, row.IdClass_value, nodesIn);
				}
			}
		});
	}
	function appendNode(node, text, cardId, className, nodesIn) {
		var n = node.appendChild({
			nodeType: 'node',
			text: text,
			cardId: cardId,
			className: className,
			checked: false,
			expanded: false,
			nodesIn: nodesIn,
			children: []
		});
		n.commit();
	}
	function loadRelations(node, className, domainName, relations, nodesIn) {
		for (var i = 0; i < relations.length; i ++) {
			var row = relations[i];
			var text = domainName + " - " + row.dst_code + " - " + row.dst_desc;
			appendNode(node, text, row.dst_id, className, nodesIn);
		}
	}
	function loadForDomainChildren(node, nodesIn, callBack) {
		if (nodesIn.length == 0) {
			callBack();
			return;
		}
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var parameters = {};
		parameters[parameterNames.CARD_ID] = node.get("cardId");
		parameters[parameterNames.CLASS_NAME] = node.get("className");//nodesIn[i].targetClassName;
		var domain = _CMCache.getDomainByName(nodesIn[0].domainName);
		parameters[parameterNames.DOMAIN_ID] = domain.get("id");
		console.log("domain = " + nodesIn[0].domainName + " target class = " + nodesIn[0].targetClassName + " class = " + node.get("className") + " card = " + node.get("cardId") + " cioe' " + node.get("text"));
		CMDBuild.ServiceProxy.relations.getList({
			params: parameters,
			scope: this,
			success: function(operation, request, decoded) {
				if (decoded.domains.length > 0) {
					loadRelations(node, nodesIn[0].targetClassName, nodesIn[0].domainName, decoded.domains[0].relations, nodesIn[0].childNodes);
				}
				var appNodesIn = nodesIn.slice(1);
				loadForDomainChildren(node, appNodesIn, callBack);
			}
		});
	}
	function expandNodes(tree, children) {
		
		if (children.length > 0) {
			var child = children[0];
			loadForDomainChildren(child, child.get("nodesIn"), function() {
				var appChildren = children.slice(1);
				expandNodes(tree, appChildren);
			});
		}
		else {
			Ext.resumeLayouts();
		}
	}
        
})();