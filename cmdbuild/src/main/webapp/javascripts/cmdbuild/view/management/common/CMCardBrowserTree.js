(function() {
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
		onCardBrowserTreeItemExpand: Ext.emptyFn
	});

	Ext.define('CMDBuild.model.CMCardBrowserNodeModel', {
		extend : 'Ext.data.Model',
		fields : [{
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
			name: 'expansibleDomains',
			type: 'auto'
		}],

		getCardId: function() {
			return this.get("cardId");
		},

		getClassName: function() {
			return this.get("className");
		},

		getExpansibleDomains: function() {
			return this.get("expansibleDomains") || [];
		}
	});

	Ext.define("CMDBuild.view.management.CMCardBrowserTree", {
		extend: "Ext.tree.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.CMCardBrowserTreeDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.useArrows = true;
			this.rootVisible = false;
			this.multiSelect = true;
			this.folderSort = false;
			this.frame = false;
			this.border = false;
			this.bodyBorder = false;

			this.store = Ext.create('Ext.data.TreeStore', {
				model : "CMDBuild.model.CMCardBrowserNodeModel",
				root : {
					expanded : true,
					children : []
				}
			});

			this.listeners = {
				checkchange: function(node, checked) {
					this.callDelegates("onCardBrowserTreeCheckChange", [this, node, checked]);
				},

				beforeitemexpand: function(node) {
					if (node._alreadyExpanded) {
						return;
					}

					node.removeAll();
					node._alreadyExpanded = true;
				},

				afteritemexpand: function(node) {
					if (node._alreadyLoaded) {
						return;
					}

					node.removeAll();
					node._alreadyLoaded = true;
					this.callDelegates("onCardBrowserTreeItemExpand", [this, node]);
				},
				scope: this
			},

			this.callParent(arguments);
		}
	});
})();