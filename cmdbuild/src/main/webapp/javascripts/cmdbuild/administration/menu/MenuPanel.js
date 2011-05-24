Ext.define("CMDBuild.Administration.MenuPanel", {
	extend: "Ext.Panel",
	alias: "menupanel",
	treePanelUrl: 'services/json/schema/modmenu/getmenu',
	avaiableMenuUrl: 'services/json/schema/modmenu/getavailableitemsmenu',
	translation : CMDBuild.Translation.administration.modmenu,
	buttonAlign : 'center',
	
	initComponent : function() {
		var _this = this;
		this.groupId = -1,

		this.deleteAction = new Ext.Action({
			iconCls : 'delete',
			text : this.translation.delete_menu,
			handler : this.askConfirmToDeleteMenu,
			scope : this
		}),

		this.modifyAction = new Ext.Action({
			iconCls : 'modify',
			text : this.translation.modify_menu,
			handler : function() {
				this.setTreesEnabled();
				this.deleteAction.disable();
				this.modifyAction.disable();
			},
			scope : this
		}),

		this.saveButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.save,
			id: 'saveMenuButton',
			name: 'saveMenuButton',
			scope: this,
			handler: this.onSave,
			disabled : true
		}),

		this.abortButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			id : 'abortMenuButton',
			name : 'abortMenuButton',
			scope : this,
			handler : this.onAbort,
			disabled : true
		});
		
		this.treePanel = new Ext.tree.TreePanel({			
			enableDD : true,
			disableSort : true,
			enableEditing : true,
			rootVisible : true,			
			rootName : this.translation.menu_root,
			frame: false,
			border: false,
			root: new Ext.tree.TreeNode()
		});
		
		var ge = new Ext.tree.TreeEditor(this.treePanel, {}, {
	        allowBlank:false,
	        blankText:'',
	        selectOnFocus: false
	    });

	    this.availabletreePanel = new Ext.tree.TreePanel({	    	
	    	frame: false,
	        border: false,
	        enableDrag: true,
	        rootVisible: false,
	        root: new Ext.tree.TreeNode(),
	        dragConfig: {
		        onBeforeDrag: function(data) {
			        return (data.node.getDepth() > 1);
		        }
	        }
		});
		
		this.addField = new Ext.form.TriggerField({
			allowBlank: true,
			fieldLabel : this.translation.new_folder,
			name : 'addfield_value',
			disabled : true,
			onTriggerClick: this.onAddButton,
			triggerClass: 'trigger-add',
			width: 200,
			treePanel: this.treePanel
		});	
		
		Ext.apply(this, {
			tbar : [this.modifyAction, this.deleteAction],
			layout: 'fit',
			frame: false,
			border: false,			
            items : [ this.mainPanel = new Ext.Panel({
            	xtype: 'panel',
            	frame: true,
				border: false,
				layout:'column',
                items:[
					this.treePanelWrapper = new Ext.Panel({
						title: this.translation.custom_menu,
						columnWidth:.47,
						height: 300,
						autoScroll: true,
						frame: true,
						border: true,
						items: [{
							xtype: 'panel',
							layout: 'form',
							frame: true,
							border: false, 
							items: [this.addField]
						}, this.treePanel]				
					}), this.buttonColumn = new Ext.Panel({
	                    columnWidth:.06,
						border: false,
						layout:'hbox',
						layoutConfig: {
	                        padding:'5',
	                        pack:'center',
	                        align:'middle'
	                    },
						items: [
						    this.removeItemButton = new Ext.Button({
								xtype: 'button',				
								iconCls : "arrow_right",
								handler: this.onRemoveItem,
								scope: this
							})
						]
					}),
	                this.availabletreePanelWrapper = new Ext.Panel({
						title: this.translation.available_elements,
						columnWidth:.47,
						height: 300,
						autoScroll: true,
						frame: true,
						border: true,
						items: [this.availabletreePanel]
					})
				],
				buttonAlign: 'center',
				buttons : [this.saveButton, this.abortButton]
            })]
		});
		CMDBuild.Administration.MenuPanel.superclass.initComponent.apply(this,arguments);
		this.subscribe('cmdb-init-menu', this.loadData, this);
		this.on("resize", this.recalculateTreesHeight, this);
	},
	
	recalculateTreesHeight: function() {
		var height = this.mainPanel.getInnerHeight();
		
		this.treePanelWrapper.setHeight(height);
		this.availabletreePanelWrapper.setHeight(height);
		this.buttonColumn.setHeight(height);
	},
	
	//private
	askConfirmToDeleteMenu : function() {
		Ext.Msg.show({
			title : CMDBuild.Translation.warnings.warning_message,
			msg : CMDBuild.Translation.common.confirmpopup.areyousure,
			scope : this,
			buttons : {
				yes : true,
				no : true
			},
			fn : function(button) {
				if (button == 'yes') {
					this.deleteMenu();					
				}
			}
		});
	},
	
	//private
	onSave:  function() {
		var nodesToSend = this.getNodesToSend(this.treePanel.getRootNode());
		this.doSaveRequest(nodesToSend);
	},	

	//private
	onAbort: function() {
		this.loadData({
					groupId : this.groupId
				});
		if (this.groupId > -1) {
			this.modifyAction.enable();
			this.deleteAction.enable();
		}
		this.publish('cmdb-abortmodify-menu');
	},
	
	//private
	onAddButton: function() {
	//the scope is the button
		var val = this.getValue() || "";
		var node = new Ext.tree.TreeNode({
			text : val,
			type : 'folder',
			subtype : 'folder',
			iconCls : 'cmdbuild-tree-folder-icon',
			leaf : false
		});
		this.treePanel.getRootNode().appendChild(node);
		this.reset();
	},
	
	//private
	doSaveRequest: function(nodesToSend) {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modmenu/savemenu',
			params : {
				group : this.groupId,
				menuItems : Ext.util.JSON.encode(nodesToSend)
			},			
			scope : this,
			success : function(form, action) {
				CMDBuild.LoadMask.get().hide();
				this.setTreesDisabled();
				this.modifyAction.setDisabled(false);
				this.deleteAction.setDisabled(false);
				this.setTreesDisabled();
				this.loadData({
					groupId : this.groupId
				});
			},
			failure : function(form, action) {
				CMDBuild.LoadMask.get().hide();
				this.modifyAction.setDisabled(true);
				this.deleteAction.setDisabled(true);
				this.publish('cmdb-abortmodify-menu');
			}
		});
	},
	
	loadData: function(params) {
		this.setTreesDisabled();
		this.groupId = params.groupId || -1;
		this.namageEditability();
		
		this.doLoadRequest({
			url: this.avaiableMenuUrl,
			tree: this.availabletreePanel,
			processTree: function(tree) {
				var children = tree.childNodes || [];
				for (var i=0, l=children.length; i<l; ++i) {
					var c = children[i];
					if (c) {
						c.setText(CMDBuild.Translation.administration.modmenu.availabletree[c.text]);
						new Ext.tree.TreeSorter(c);
					}
				}
			}
		});
		this.doLoadRequest({
			url: this.treePanelUrl,
			tree: this.treePanel
		});		
	},
	
	namageEditability: function() {
		if (this.groupId < 0) {
			this.modifyAction.setDisabled(true);
			this.deleteAction.setDisabled(true);
		} else {
			this.modifyAction.setDisabled(false);
			this.deleteAction.setDisabled(false);
		}
	},
	
	/**
	 * params must have tree, url, and a function to process the result
	 * */
	doLoadRequest: function(params) {
		var processTree = params.processTree || Ext.emptyFn;
		CMDBuild.LoadMask.get().show();
    	CMDBuild.Ajax.request({
			method : 'GET',
			url : params.url,
			params: {
    			group: this.groupId
			},
			scope: this,
			success : function(response, options, decoded) {
				var itemsMap = CMDBuild.TreeUtility.arrayToMap(decoded);
				var newTree = CMDBuild.TreeUtility.buildTree(itemsMap, "menu", addAttributes = true);
				newTree.setText(this.translation.menu_root);
				processTree(newTree);
				var root = params.tree.setRootNode(newTree);
				root.expand();
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	},
	
	setTreesDisabled : function() {
		this.treePanelWrapper.disable();
		this.availabletreePanelWrapper.disable();
		this.saveButton.disable();
		this.abortButton.disable();
		this.addField.disable();
		this.removeItemButton.disable();
	},

	setTreesEnabled : function() {
		this.treePanelWrapper.enable();
		this.availabletreePanelWrapper.enable();
		this.abortButton.enable();
		this.saveButton.enable();
		this.addField.enable();
		this.removeItemButton.enable();
	},

	deleteMenu: function() {
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modmenu/deletemenu',
			params : {
				group : this.groupId
			},
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			scope : this,
			success : function(form, action) {
				this.loadData({
					groupId : this.groupId
				});
				this.modifyAction.setDisabled(false);
				this.deleteAction.setDisabled(false);
				this.setTreesDisabled();
			},
			failure : function(form, action) {
				this.modifyAction.setDisabled(true);
				this.deleteAction.setDisabled(true);
				this.publish('cmdb-abortmodify-menu');

			}
		});
	},
	
	onRemoveItem: function() {
		var tree = this.treePanel;
		var sm = tree.getSelectionModel();
		var node = sm.getSelectedNode();
		if (node && node.attributes.type) {
			this.removeTreeBranch(node);
		}
	},
	
	removeTreeBranch : function(node) {
		while (node.hasChildNodes()) {
			this.removeTreeBranch(node.childNodes[0]);
		}
		
		var nodeType;
		if (node.attributes.type.match("report")) {
			nodeType = "report";
		} else {
			nodeType = node.attributes.type;
		}
		var originalFolderOfTheLeaf = CMDBuild.TreeUtility.searchNodeByAttribute({
			attribute: "id",
			value: "available"+nodeType,
			root: this.availabletreePanel.getRootNode()
		});
		//remove the node before adding it to the original tree
		node.remove();
		if (originalFolderOfTheLeaf) {
			var newNode = new Ext.tree.TreeNode(node.attributes);
			originalFolderOfTheLeaf.expand();
			originalFolderOfTheLeaf.appendChild(newNode);
		}
	},
	
	getNodesToSend : function(node) {
		var nodesToSend = [];	
		var canBeParent = false;
		
		if (node.attributes.type) {
			// a node without type isn't a menu item,
			// for instance the menu fake root
			nodesToSend.push(node.attributes);
			canBeParent = true;
		}
		
		for (var i=0, len=node.childNodes.length; i<len; ++i) {
			var child = node.childNodes[i];
			if (canBeParent) {
				child.attributes.parent = node.id;
			} else {
				delete child.attributes.parent;
			}
			nodesToSend = nodesToSend.concat(this.getNodesToSend(child));
		}
		
		return nodesToSend;
    }
});