(function() {
	var index = 0;

	Ext.define("CMDBuild.controller.administration.menu.CMModMenuController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);

			this.menutree = this.view.mp.treePanel;
			this.availableItemsTree = this.view.mp.availabletreePanel;

			this.view.mp.saveButton.on("click", onSaveButtonClick, this);
			this.view.mp.abortButton.on("click", onAbortButtonClick, this);
			this.view.mp.deleteButton.on("click", onDeleteButtonClick, this);
			this.view.mp.addFolderField.on("cm-add-folder-click", onAddFolderFieldClick, this)
		},

		onViewOnFront: function(menu) {
			if (menu) {
				this.currentMenu = menu;
				this.currentMenuId = menu.get("id");
				this.loadMenuTree(this.currentMenuId);
				this.loadAvailableItemsTree(this.currentMenuId);
			}
		},

		loadMenuTree: function() {
			this.doLoadRequest({
				url: 'services/json/schema/modmenu/getmenu',
				tree: this.menutree,
				sort: true
			});
		},

		loadAvailableItemsTree: function() {
			this.doLoadRequest({
				url: 'services/json/schema/modmenu/getavailableitemsmenu',
				tree: this.availableItemsTree,
				sort: true
			});
		},

		doLoadRequest: function(params) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				method : 'GET',
				url : params.url,
				params: {
					group: this.currentMenuId
				},
				scope: this,
				success : function(response, options, decoded) {
					var itemsMap = CMDBuild.TreeUtility.arrayToMap(decoded);
					processTree.call(this, itemsMap, params.tree, params.sort);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});
	
	function onSaveButtonClick() {
		var nodesToSend = getNodesToSend(this.menutree.getRootNode());
		doSaveRequest.call(this, nodesToSend);
	}
	
	function onAbortButtonClick() {
		this.onViewOnFront(this.currentMenu);
	}
	
	function onDeleteButtonClick() {
		Ext.Msg.show({
			title : CMDBuild.Translation.warnings.warning_message,
			msg : CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteMenu.call(this);
				}
			}	
		});
	}
	
	function onAddFolderFieldClick(v) {
		this.menutree.getRootNode().appendChild({
			text : v,
			type : 'folder',
			subtype : 'folder',
			iconCls : 'cmdbuild-tree-folder-icon',
			leaf : false
		});
	}
	
	function processTree (itemsMap, tree, sort) {
		var nodesMap = {};
		var out = [];

		for (var key in itemsMap) {
			var nodeConf =  buildNodeConf(itemsMap[key]);
			nodesMap[nodeConf.id] = nodeConf;
		}

		for (var id in nodesMap) {
			var node = nodesMap[id];
			if (node.parent && nodesMap[node.parent]) {
				linkToParent(node, nodesMap);
			} else {
				out.push(node);
			}
		}

		var root = tree.store.getRootNode();
		root.removeAll();

		if (out.length > 0) {
			root.appendChild(out);
		}

		if (sort) {
			tree.store.sort();
		}
	}

	function buildNodeConf(node) {
		var type = node.type,
			superclass = node.superclass;

		return {
			id: node.id,
			text: CMDBuild.Translation.administration.modmenu.availabletree[node.text] || node.text,
			subType: node.subType,
			type: node.type,
			leaf: node.leaf,
			parent: node.parent,
			objid: node.objid,
			subtype: node.subtype,
			cmIndex: node.cmIndex,
			iconCls: "cmdbuild-tree-" + (superclass ? "super" : "") + type +"-icon"
		};
	}

	function linkToParent(node, nodesMap) {
		var parentNode = nodesMap[node.parent];
		parentNode.children = (parentNode.children || []);
		parentNode.children.push(node);
		parentNode.leaf = false;
	}

	function deleteMenu () {
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modmenu/deletemenu',
			params : {
				group : this.currentMenuId
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope : this,
			callback: function() {
				this.loadMenuTree(this.currentMenuId);
				this.loadAvailableItemsTree(this.currentMenuId);
			}
		});
	}

	function getNodesToSend(node) {
		var nodesToSend = [];
		var canBeParent = false;

		if (node.get("type")) {
			// a node without type isn't a menu item,
			// for instance the menu fake root
			var data = {};
			nodesToSend.push({
				id: node.get("id") != "" ? node.get("id") : node.id,
				type: node.get("type"),
				parent: node.parentNode.get("id") != "" ? node.parentNode.get("id") : node.parentNode.id,
				leaf: node.get("leaf"),
				iconCls: node.get("iconCls"),
				text: node.get("text"),
				// report only
				objid: node.get("objid"),
				subtype: node.get("subtype"),
				cmIndex: index++
			});
		}

		for (var i=0, len=node.childNodes.length; i<len; ++i) {
			var child = node.childNodes[i];
			child.set("parent", node.id);
			nodesToSend = nodesToSend.concat(getNodesToSend(child));
		}

		return nodesToSend;
	}

	function doSaveRequest(nodesToSend) {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modmenu/savemenu',
			params : {
				group : this.currentMenuId,
				menuItems : Ext.JSON.encode(nodesToSend)
			},			
			scope : this,
			callback: function() {
				CMDBuild.LoadMask.get().hide();
				this.loadMenuTree(this.currentMenuId);
				this.loadAvailableItemsTree(this.currentMenuId);
			}
		});
	}
})();