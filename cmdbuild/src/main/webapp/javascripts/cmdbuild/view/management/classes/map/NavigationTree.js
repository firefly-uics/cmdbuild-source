(function() {

	Ext.define("CMDBuild.view.management.classes.map.NavigationTreeDelegate", {});

	Ext.define("CMDBuild.view.management.classes.map.NavigationTree", {
		extend : "Ext.tree.Panel",

		mixins : {
			delegable : "CMDBuild.core.CMDelegable"
		},

		constructor : function(ds) {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.view.management.classes.map.NavigationTreeDelegate");

			this.dataSource = ds || null;
			this.callParent(arguments);
		},

		initComponent : function() {
			this.useArrows = true;
			this.rootVisible = true;
			this.multiSelect = false;
			this.folderSort = false;
			this.frame = false;
			this.border = false;
			this.bodyBorder = false;
			this.hideHeaders = true;

			this.activationCount = 0;
			var me = this;
			var SHOW_ICON = 'images/icons/bullet_go.png';
			var HIDE_ICON = 'images/icons/cancel.png';

			this.tbar = [ '->', {
				iconCls : "arrow_refresh",
				handler : function() {
					me.dataSource.refresh();
				}
			} ];

			this.columns = [ {
				xtype : 'treecolumn',
				flex : 2,
				sortable : false,
				dataIndex : 'text',
				menuDisabled : true
			}, {
				width : 40,
				menuDisabled : true,
				xtype : 'actioncolumn',
				tooltip : CMDBuild.Translation.management.modcard.open_relation,
				align : 'center',
				sortable : false,
				icon : 'images/icons/bullet_go.png',
				handler : function(grid, rowIndex, colIndex, actionItem, event, record, row) {
					me.navigateOnCard(record);
				},
				isDisabled : function(view, rowIdx, colIdx, item, record) {
					return false;
				}
			} ];
			this.store = Ext.create('Ext.data.TreeStore', {
				model : "CMDBuild.view.management.classes.map.NavigationTreeModel",
				root : {
					expanded : true,
					text : me.rootText,
					children : []
				}
			});

			this.interactionDocument.observe(this);

			this.callParent(arguments);

			// Force to not select via UI
			this.mon(this, "beforeselect", function() {
				return false;
			}, this);

			this.mon(this, "checkchange", function(node, checked) {
				checkNodeChildren(node, checked);
				var allNodes = getAllNodes(this.getRootNode());;
				this.interactionDocument.setNavigables(allNodes);
			}, this);

			this.mon(this, "activate", function(treePanel) {
				// this.callDelegates("onCardBrowserTreeActivate", [this,
				// ++this.activationCount]);
			}, this);

			this.mon(this, "itemappend", function(tree, node) {
			}, this);
		},
		getNavigationClasses : function(node, classes) {
			var name = node.get("className");
			if (name && classes.indexOf(name) == -1) {
				classes.push(name);
			}
			var children = node.childNodes || node.children || [];
			for (var i = 0, l = children.length; i < l; ++i) {
				this.getNavigationClasses(children[i], classes);
			}
		},
		refresh : function() {
			checkParents(this.interactionDocument.getNavigableToOpen());
			this.interactionDocument.resetNavigableToOpen();
		},
		loaded : function() {
			var allNodes = getAllNodes(this.getRootNode());
			var classesControlledByNavigation = [];
			this.getNavigationClasses(this.getRootNode(), classesControlledByNavigation);
			this.interactionDocument.setClassesControlledByNavigation(classesControlledByNavigation);
			this.interactionDocument.setNavigables(allNodes);
		},
		navigateOnCard : function(record) {
			var className = record.get('className');
			var type = _CMCache.getEntryTypeByName(className);
			if (!type) {
				return;
			}
			var classId = type.get("id");
			this.callDelegates("onCardNavigation", [ {
				Id : record.get('cardId'),
				IdClass : classId
			} ]);
		},
		zoomOnCard : function(record) {

			this.callDelegates("onCardZoom", [ {
				cardId : record.get("cardId"),
				className : record.get("className")
			} ]);
		},
	});
	Ext.define('CMDBuild.view.management.classes.map.NavigationTreeModel', {
		extend : 'Ext.data.Model',
		idProperty : "cardId",
		fields : [ {
			name : 'text',
			type : 'string'
		}, {
			name : 'visible',
			type : 'boolean'
		}, {
			name : 'cardId',
			type : 'string' // cardId or the name of a geoserver layer
		}, {
			name : 'className',
			type : 'string'
		}, {
			name : 'classId',
			type : 'int'
		}, {
			// to identify the exclusive nodes that
			// represent the base for the vertical overlap
			name : 'baseNode',
			type : 'boolean'
		} ]
	});
	function checkNodeChildren(node, checked) {
		var children = node.childNodes || node.children || [];
		for (var i = 0, l = children.length; i < l; ++i) {
			var child = children[i];
			child.set("checked", checked);
			checkNodeChildren(child, checked)
		}

	}
	function checkParents(node) {
		if (node) {
			node.set("checked", true);
			checkParents(node.parentNode);
		}
	}
	function getAllNodes(node) {
		var allNodes = [];
		getAllNodesRecursive(node, allNodes);
		return allNodes;
	}
	function getAllNodesRecursive(node, allNodes) {
		var children = node.childNodes || node.children || [];
		for (var i = 0, l = children.length; i < l; ++i) {
			var child = children[i];
			allNodes.push(child);
			getAllNodesRecursive(child, allNodes);
		}
	}
})();