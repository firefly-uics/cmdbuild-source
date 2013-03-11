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
			this.view.mp.addFolderField.on("cm-add-folder-click", onAddFolderFieldClick, this);
		},

		// override
		onViewOnFront: function(menu) {
			if (menu) {
				this.currentMenu = menu;
				this.currentMenuName = menu.get("name");
				loadMenuTree(this);
				loadAvailableItemsTree(this);
			}
		}
	});

	function loadMenuTree(me) {
		CMDBuild.LoadMask.get().show();
		_CMProxy.menu.readConfiguration({
			params: getCallParams(me),
			success : function(response, options, decoded) {
				var menu = adapt(decoded.menu);
				_debug(menu);
				var root = me.menutree.getRootNode();
				root.removeAll();
				if (menu.children) {// if not defined has no children field
					root.appendChild(menu.children);
					me.menutree.store.sort([{
						property : 'index',
						direction: 'ASC'
					}]);
				}
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function loadAvailableItemsTree(me) {
		CMDBuild.LoadMask.get().show();
		_CMProxy.menu.readAvailableItems({
			params: getCallParams(me),
			success : function(response, options, decoded) {
				var menu = adapt(decoded.menu);
				_debug(menu);
				var root = me.availableItemsTree.getRootNode();
				root.removeAll();
				root.appendChild(menu.children);
				me.menutree.store.sort([{
					property : 'index',
					direction: 'ASC'
				}, {
					property : 'text',
					direction: 'ASC'
				}]);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onSaveButtonClick() {
		doSaveRequest(this);
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
			text: v,
			type: 'folder',
			subtype: 'folder',
			iconCls: 'cmdbuild-tree-folder-icon',
			leaf: false
		});
	}

	function adapt(menu) {
		var out = adaptSingleNode(menu);
		if (menu.children || out.type == "folder") {
			out.leaf = false;
			out.children = [];
			out.expanded = true;

			var children = menu.children || [];
			for (var i=0, l=children.length; i<l; ++i) {
				var child = children[i];
				out.children.push(adapt(child));
			}
		} else {
			out.leaf = true;
		}

		return out;
	}

	function adaptSingleNode(node) {
		var out = {};
		var superclass = false;
		var type = node.type;
		var text = node.description;
		if (type == "class" 
			|| type == "processclass") {

			entryType = _CMCache.getEntryTypeByName(node.referencedClassName);
			if (entryType) {
				superclass = entryType.isSuperClass();
			}
		}

		// hack to manage the folder categories of
		// the available menu items
		if (type == "folder") {
			text = CMDBuild.Translation.administration.modmenu.availabletree[node.description] || node.description;
			out.folderType = node.description;
		}

		out.type = type;
		out.index = node.index;
		out.referencedClassName = node.referencedClassName;
		out.referencedElementId = node.referencedElementId;
		out.text = text;
		out.iconCls = "cmdbuild-tree-" + (superclass ? "super" : "") + type +"-icon";

		return out;
	}

	function deleteMenu () {
		var me = this;

		_CMProxy.menu.remove({
			params: getCallParams(me),
			waitTitle: CMDBuild.Translation.common.wait_title,
			waitMsg: CMDBuild.Translation.common.wait_msg,
			callback: function() {
				loadMenuTree(me);
				loadAvailableItemsTree(me);
			}
		});
	}

	function getMenuConfiguration(node) {
		var menuConfiguration = toServer(node);
		menuConfiguration.index = 0;

		if (node.childNodes.length > 0) {
			menuConfiguration.children = [];
			for (var i=0, len=node.childNodes.length; i<len; ++i) {
				var child = node.childNodes[i];
				child.set("parent", node.id);
				var childConf = getMenuConfiguration(child);
				childConf.index = i;
				menuConfiguration.children.push(childConf);
			}
		}

		return menuConfiguration;
	}

	function toServer(node) {

		var out = {
			type: node.get("type"),
			description: node.get("text"),
			referencedClassName: "",
			referencedElementId: ""
		};

		if (isTable(out.type)) {
			// TODO, when uniform the serialization between available and defined items
			// remove this check, and use directly the node.get("referencedClassName");
			var referencedClassName = node.get("referencedClassName");
			if (referencedClassName && referencedClassName != "") {
				out.referencedClassName = referencedClassName;
			} else {
				out.referencedClassName = _CMCache.getEntryTypeNameById(node.get("id"));
			}
		}

		if (isReport(out.type) || isDashboard(out.type)) {
			// TODO, when uniform the serialization between available and defined items
			// remove this check, and use directly the node.get("referencedClassName");
			var referencedElementId = node.get("referencedElementId");
			if (!referencedElementId) {
				referencedElementId = node.get("objid");
			}

			out.referencedElementId = referencedElementId;
		}

		return out;
	}

	function doSaveRequest(me) {
		var menuTree = getMenuConfiguration(me.menutree.getRootNode());
		menuTree.type = "root";

		var parameterNames = _CMProxy.parameter;
		var params = getCallParams(me);
		params[parameterNames.MENU] = Ext.encode(menuTree);

		CMDBuild.LoadMask.get().show();
		_CMProxy.menu.save({
			params: params,
			callback: function() {
				CMDBuild.LoadMask.get().hide();
				loadMenuTree(me);
				loadAvailableItemsTree(me);
			}
		});
	}

	function getCallParams(me) {
		var parameterNames = _CMProxy.parameter;
		var params = {};
		params[parameterNames.GROUP_NAME] = me.currentMenuName;

		return params;
	}

	function isDashboard(type) {
		return type == "dashboard";
	}

	function isReport(type) {
		return type == "reportpdf" || type == "reportcsv";
	}

	function isTable(type) {
		return type == "class" || type == "processclass";
	}

})();