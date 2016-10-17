(function() {

	Ext.define("CMDBuild.view.management.classes.map.NavigationTreeDelegate", {});

	Ext
			.define("CMDBuild.view.management.classes.map.NavigationTree",
					{
						extend : "Ext.tree.Panel",

						mixins : {
							delegable : "CMDBuild.core.CMDelegable"
						},

						oldCard : undefined,

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
								Ext.suspendLayouts();
								checkNodeChildren(node, checked);
								Ext.resumeLayouts();
								var allNodes = getAllNodes(this.getRootNode());
								this.interactionDocument.setNavigables(allNodes);
							}, this);

							this.mon(this, "activate", function(treePanel) {
								this.refresh();
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
							var me = this;
							setTimeout(me._refresh(), 10);
						},
						_refresh : function() {
							var card = this.interactionDocument.getCurrentCard();
							if (!card || card.cardId === -1) {
								return;
							}
							if (this.oldCard && this.oldCard.className === card.className
									&& this.oldCard.cardId == card.cardId) {
								return;
							}
							this.oldCard = card;
							var navigable = this.interactionDocument.getNavigable(card);
							if (navigable) {
								Ext.suspendLayouts();
								check(navigable.node, true);
								Ext.resumeLayouts();

								selectNode(this, navigable.node);
							}
						},
						loaded : function() {
							var allNodes = getAllNodes(this.getRootNode());
							this.interactionDocument.setStarted(true);
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
			type : 'int'
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
	function check(node, checked) {
		node.set('checked', checked);
		var isBase = node.get("baseNode");
		checkParentNodes(node, checked, false);
		checkBrotherNodes(node, !isBase);
	}
	function checkParentNodes(node, checked, overBase) {
		var parentNode = node.parentNode;
		if (parentNode) {
			parentNode.set('checked', checked);
			var isBase = parentNode.get("baseNode");
			if (isBase) {
				checkParentNodes(parentNode, checked, true);
				checkBrotherNodes(parentNode, false);
			} else if (overBase) {
				checkParentNodes(parentNode, checked, true);
			} else {
				checkParentNodes(parentNode, checked, false);
				checkBrotherNodes(parentNode, true);
			}
		}
	}
	function checkBrotherNodes(node, checked) {
		var parentNode = node.parentNode;
		if (!parentNode) {
			return;
		}
		var children = parentNode.childNodes || parentNode.children || [];
		for (var i = 0, l = children.length; i < l; ++i) {
			var child = children[i];
			if (child !== node) {
				child.set('checked', checked);
				checkNodeChildren(child, checked);
			}

		}
	}
	function checkNodeChildren(node, checked) {
		var children = node.childNodes || node.children || [];
		for (var i = 0, l = children.length; i < l; ++i) {
			var child = children[i];
			child.set("checked", checked);
			checkNodeChildren(child, checked);

		}

	}
	function selectNode(tree, node) {
		if (!node) {
			return;
		}
		var navigableNode = tree.interactionDocument.getNavigableNode({
			className : node.get("className"),
			cardId : node.get("cardId")
		});
		if (navigableNode === null) {
			return;
		}
		if (navigableNode.parentNode && navigableNode.parentNode.childNodes) {
			var children = navigableNode.parentNode.childNodes || navigableNode.parentNode.children || [];
			if (children.length > CMDBuild.gis.constants.navigationTree.limitSelection) {
				node = navigableNode.parentNode;
			}
		}
		var cb = Ext.Function.createDelayed(function() {
			deselectAllSilently(tree);
			var nodeEl = Ext.get(tree.view.getNode(node));
			nodeEl.scrollIntoView(tree.view.el, false, false);
			selectNodeSilently(tree, node);
		}, 500);
		var path = node.getPath();
		tree.selectPath(path, undefined, undefined, cb);
	}
	function deselectAllSilently(me) {
		try {
			var sm = me.getSelectionModel();
			if (sm) {
				var suppressEvent = true;
				sm.deselectAll(suppressEvent);
			}
		} catch (e) {
			_debug("ERROR deselecting the CardBrowserTree", e);
		}
	}
	function selectNodeSilently(tree, node) {
		if (!node) {
			return;
		}

		try {
			var sm = tree.getSelectionModel();
			if (sm) {
				sm.suspendEvents();
				sm.select(node);
				sm.resumeEvents();
			}
		} catch (e) {
			_debug("ERROR selecting the CardBrowserTree", e);
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