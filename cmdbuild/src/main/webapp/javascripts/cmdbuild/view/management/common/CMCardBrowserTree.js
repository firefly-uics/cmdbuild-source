(function() {

	Ext.define('CMDBuild.model.CMCardBrowserNodeModel', {
		extend : 'Ext.data.Model',
		fields : [{
			name : 'text', type : 'string'
		}, {
			name : 'visible', type : 'boolean'
		}, {
			name : 'cardId', type : 'int'
		}, {
			name : 'className', type : 'string'
		}, {
			name : 'classId', type : 'int'
		}, {
			name: 'expansibleDomains', type: 'auto'
		}, {
			name: 'childrenLoaded', type: 'boolean'
		}],

		getCardId: function() {
			return this.get("cardId");
		},

		getCMDBuildClassId: function() {
			return this.get("classId");
		},

		getCMDBuildClassName: function() {
			return this.get("className");
		},

		getExpansibleDomains: function() {
			return this.get("expansibleDomains") || [];
		},

		didChildrenLoaded: function() {
			return this.get("childrenLoaded");
		},

		addLoadedChildren: function(child) {
			this.set("childrenLoaded", true);
			return this.appendChild(child);
		},

		isBindingCard: function(card) {
			var out = false;
			if (card) {
				out = this.getCardId() == card.get("Id")
					&& this.getCMDBuildClassId() == card.get("IdClass");
			}

			return out;
		}
	});

	Ext.define("CMDBuild.view.management.CMCardBrowserTreeDelegate", {
		/**
		 * 
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The tree who call the method
		 * @param {Ext.data.NodeInterface} node The node which has changed his check
		 * @param {Boolean} checked the state of the check
		 */
		onCardBrowserTreeCheckChange: Ext.emptyFn,

		/**
		 * 
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The tree who call the method 
		 * @param {Ext.data.NodeInterface} node The node which was expanded
		 */
		onCardBrowserTreeItemExpand: Ext.emptyFn,

		/**
		 * Called when click over the specific icon
		 * @param {object} cardBaseInfo, an object with Id and IdClass for the selected card
		 */
		onCardBrowserTreeCardSelected: Ext.emptyFn,

		/**
		 * Called after a new node is added
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The tree who call the method
		 * @param {Ext.data.NodeInterface} targetNode The node that receive the new child
		 * @param {Ext.data.NodeInterface} childNode The inserted node
		 */
		onCardBrowserTreeItemAdded: Ext.emptyFn,

		/**
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The activated panel
		 * @param {integer} activationCount The number of times the panel was activated
		 */
		onCardBrowserTreeActivate: Ext.empfyFn
	});

	Ext.define("CMDBuild.view.management.CMCardBrowserTree", {
		extend: "Ext.tree.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function(ds) {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.CMCardBrowserTreeDelegate");

			this.dataSource = ds || null;
			this.callParent(arguments);
		},

		initComponent: function() {
			this.useArrows = true;
			this.rootVisible = false;
			this.multiSelect = false;
			this.folderSort = false;
			this.frame = false;
			this.border = false;
			this.bodyBorder = false;

			this.activationCount = 0;

			var me = this;
			this.columns = [{
				xtype : 'treecolumn',
				text : '@@ Card Description',
				flex : 2,
				sortable : true,
				dataIndex : 'text'
			}, {
				width : 40,
				menuDisabled : true,
				xtype : 'actioncolumn',
				tooltip : '@@ Select this card',
				align : 'center',
				icon : 'images/icons/bullet_go.png',
				handler : function(grid, rowIndex, colIndex, actionItem,
						event, record, row) {

					// to highlight the node
					me.selectNodeSilently(record);

					me.callDelegates( "onCardBrowserTreeCardSelected", {
						Id : record .get("cardId"),
						IdClass : record .get("classId")
					});
				},
				isDisabled : function(view, rowIdx, colIdx, item, record) {
					return false;
				}
			}];

			this.store = Ext.create('Ext.data.TreeStore', {
				model : "CMDBuild.model.CMCardBrowserNodeModel",
				root : {
					expanded : true,
					children : []
				}
			});

			this.listeners = {

				afteritemexpand: function(node) {
					this.callDelegates("onCardBrowserTreeItemExpand", [this, node]);
				},

				// Force to not select via UI
				beforeselect: function() {
					return false;
				},

				checkchange: function(node, checked) {
					this.callDelegates("onCardBrowserTreeCheckChange", [this, node, checked]);
				},

				activate: function(treePanel) {
					this.callDelegates("onCardBrowserTreeActivate", [this, ++this.activationCount]);
				},

				scope: this
			},

			this.callParent(arguments);
		},

		setDataSource: function(ds) {
			this.dataSource = ds;
		},

		selectCardSilently: function(card) {
			deselectAllSilently(this);

			if (!card) {
				return;
			}

			var r = this.getRootNode();
			if (r) {
				var node = r.findChildBy( function(child) {
						return child.isBindingCard(card);
					}, null, true);

				this.selectNodeSilently(node);
			}
		},

		selectNodeSilently: function(node) {
			if (!node) {
				return;
			}

			try {
				var sm = this.getSelectionModel();
				if (sm) {
					sm.suspendEvents();
					sm.select(node);
					sm.resumeEvents();
				}
			} catch (e) {
				_debug("ERROR selecting the CardBrowserTree", e);
			}
		},

		addChildToNode: function(targetNode, childNode) {
			var node = null;

			if (targetNode && childNode) {
				node = targetNode.appendChild(childNode);

				targetNode.sort( function(a, b) {
					var textA = a.get("text");
					var textB = b.get("text");

					if (textA > textB) {
						return 1;
					} else if (textA < textB) {
						return -1;
					}
					return 0;
				});

				this.callDelegates("onCardBrowserTreeItemAdded", [this, targetNode, node]);
			}

			return node;
		},

		addLoadedChildren: function(targetNode, child) {
			targetNode.set("childrenLoaded", true);
			return this.addChildToNode(targetNode, child);
		}
	});

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
})();