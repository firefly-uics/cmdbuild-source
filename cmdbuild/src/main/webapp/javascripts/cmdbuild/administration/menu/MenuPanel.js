(function() {
	var tr = CMDBuild.Translation.administration.modmenu;
	
	Ext.define("MenuStore", {
		extend: "Ext.data.TreeStore",
		fields: [
			{name: "type", type: "string"},
			{name: "subType", type: "string"},
			{name: "text", type: "string"},
			{name: "id", type: "string"},
			{name: "parent", type: "string"}
		],
		root : {
			expanded : true,
			children : []
		}
	})
	
Ext.define("CMDBuild.Administration.MenuPanel", {
	extend: "Ext.panel.Panel",
	initComponent : function() {
		var _this = this;
		this.groupId = -1,

		this.deleteButton = new Ext.button.Button({
			iconCls : 'delete',
			text : tr.delete_menu,
			handler : this.askConfirmToDeleteMenu,
			scope : this
		}),

		this.saveButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.save,
			id: 'saveMenuButton',
			name: 'saveMenuButton',
			scope: this,
			handler: this.onSave
		}),

		this.abortButton = new Ext.button.Button({
			text : CMDBuild.Translation.common.buttons.abort,
			id : 'abortMenuButton',
			name : 'abortMenuButton',
			scope : this,
			handler : this.onAbort
		});

		this.treePanel = Ext.create("Ext.tree.Panel", {
			title: tr.custom_menu,
			store: new MenuStore(),
			border: true,
			rootVisible: false,
			viewConfig : {
				plugins : {
					ptype : 'treeviewdragdrop'
				}
			},
			flex: 9
		});

		this.availabletreePanel = Ext.create("Ext.tree.Panel", {
			title: tr.available_elements,
			store: new MenuStore(),
			border: true,
			rootVisible: false,
			viewConfig : {
				plugins : {
					ptype : 'treeviewdragdrop',
					enableDrop: false
				}
			},
			flex: 9
		});

		this.removeFromMeneButton = new Ext.button.Button({
			iconCls : "arrow_right",
			handler: this.onRemoveItem,
			scope: this
		})

		this.addFolderField = new Ext.form.TriggerField({
			allowBlank: true,
			fieldLabel : tr.new_folder,
			name : 'addfield_value',
			onTriggerClick: function() {
				this.fireEvent("cm-add-folder-click", this.getValue());
			},
			triggerCls: 'trigger-add',
			treePanel: this.treePanel
		});

		Ext.apply(this, {
			border : false,
			frame : true,
			tbar : [this.addFolderField, this.deleteButton],
			buttons: [this.saveButton, this.abortButton],
			layout: {
				type: 'hbox',
				align:'stretch'
			},
			items: [
				this.treePanel,
				{
					xtype: "panel",
					frame: false,
					bodyCls: "x-panel-body-default-framed",
					layout: {
						type:'vbox',
						pack:'center',
						align:'center'
					},
					border: false,
					items: [this.removeFromMeneButton],
					margin: "3"
				},
				this.availabletreePanel
			]
		});
		
		this.callParent(arguments);
	},

	onRemoveItem: function() {
		var tree = this.treePanel;
		var sm = tree.getSelectionModel();
		var node = sm.getSelection()[0];

		if (node && node.get("type")) {
			this.removeTreeBranch(node);
		}
	},
	
	removeTreeBranch : function(node) {
		while (node.hasChildNodes()) {
			this.removeTreeBranch(node.childNodes[0]);
		}
		var nodeType = node.get("type");
		if (nodeType.match("report")) {
			nodeType = "report";
		}
		var availableTreeRoot = this.availabletreePanel.getRootNode();
		var originalFolderOfTheLeaf = availableTreeRoot.findChild("id", "available"+nodeType);
		//remove the node before adding it to the original tree
		node.remove();
		if (originalFolderOfTheLeaf) {
			originalFolderOfTheLeaf.expand();
			originalFolderOfTheLeaf.appendChild(node);
		}
	},
	
	onMenuSelected: function() {

	}
});
})()