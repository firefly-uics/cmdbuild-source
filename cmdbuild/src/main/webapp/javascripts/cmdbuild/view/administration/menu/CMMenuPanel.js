(function() {
	var cellEditor = Ext.create('Ext.grid.plugin.CellEditing', {
		clicksToEdit: 2,
		autoCancel: false,

		listeners: {
			// Prevent to edit root node
			beforeedit: function(editor, context, eOpts) {
				if (context.record.isRoot())
					return false;
			}
		}
	});
	var tr = CMDBuild.Translation.administration.modmenu;
	var warningNoSaved = CMDBuild.Translation.warnings.saveMenuBeforeAccess;

	Ext.define("MenuStore", {
		extend: "Ext.data.TreeStore",
		fields: [
			{name: "type", type: "string"},
			{name: "text", type: "string"},
			{name: "index", type: "ingeter"},
			{name: "referencedClassName", type: "string"},
			{name: "referencedElementId", type: "string"},
			{name: "uuid", type: "string"},
			{name: "button", type: "boolean"},
			{name: "folderType", type: "string"} // used to retrieve the folder of the available items
		],
		root : {
			text: "",
			expanded : true,
			children : []
		}
	});

Ext.define("CMDBuild.Administration.MenuPanel", {
	extend: "Ext.panel.Panel",

	/**
	 * @property {Object}
	 */
	translatableAttributesConfigurationsBuffer: {},

//	statics: {
//		openTranslationsWindow: function(uuid) {
//			Ext.create('CMDBuild.controller.common.field.translatable.Window', {
//				title: CMDBuild.Translation.translations,
//				translationsKeyType: "MenuItem",
//				translationsKeyField: "Description",
//				translationsKeyName: uuid
//			});
//		},
//		warningNoSaveWindow: function() {
//			message = htmlComposeMessage(warningNoSaved);
//			CMDBuild.Msg.warn(null, message , false);
//		}
//
//	},
	initComponent : function() {
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
			rootVisible: true,
			hideHeaders: true,
			listeners: {
				selectionchange: function(tree, selected, eOpts) {
					if (selected[0]) {
						clearNodes(this.getRootNode());
						selected[0].set("button", true);
					}
				}
			},
			columns: [{
				dataIndex: "text",
				editor: {
					allowBlank: false,
					xtype: "textfield"
				},
				flex: 1,
				xtype: "treecolumn"
			},
//			{
//				width: 30,
//				align: 'center',
//				sortable: false,
//				hideable: false,
//				menuDisabled: true,
//				fixed: true,
//				renderer: renderTranslationButton
//			}
			Ext.create('Ext.grid.column.Action', {
				align: 'center',
				width: 30,
				sortable: false,
				hideable: false,
				menuDisabled: true,
				fixed: true,

//				renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
//					if (record.isRoot())
//						return '';
//				},

				items: [
					Ext.create('CMDBuild.core.buttons.FieldTranslation', {
						scope: this,

						handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
							Ext.create('CMDBuild.controller.common.field.translatable.NoFieldWindow', {
								buffer: this.translatableAttributesConfigurationsBuffer,
								translationFieldConfig: {
									type: 'menuitem',
									owner: record.get('uuid'), // TODO: should be group identifier
									identifier: record.get('uuid'),
									field: CMDBuild.core.proxy.Constants.DESCRIPTION
								}
							});
						},

//						getClass: function(value, metadata, record, rowIndex, colIndex, store) {
//_debug('record', record, record.isRoot());
//							if (record.isRoot())
//								return 'x-hidden';
//						},

						isDisabled: function(grid, rowIndex, colIndex, item, record) {
							return record.isRoot() || !CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].hasEnabledLanguages();
						}
					})
				]
			})
			],
			plugins : cellEditor,
			viewConfig : {
				plugins : {
					ptype: "treeviewdragdrop"
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
		});

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
			frame : false,
			cls: "x-panel-body-default-framed",
			bodyCls: 'cmgraypanel',
			tbar : [this.addFolderField, this.deleteButton],
			buttonAlign: "center",
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
		var originalFolderOfTheLeaf = availableTreeRoot.findChild("folderType", nodeType);
		//remove the node before adding it to the original tree
		node.remove();
		if (originalFolderOfTheLeaf) {
			originalFolderOfTheLeaf.expand();
			originalFolderOfTheLeaf.appendChild(node);
		}
	}
});
function clearNodes(node) {
	node.set("button", false);
	for (var i = 0; node.childNodes && i < node.childNodes.length; i++) {
		clearNodes(node.childNodes[i]);
	}
}
//function renderTranslationButton(value, metadata, record) {
//	if (! record.get("button") || ! _CMCache.isMultiLanguages()) {
//		return '<div/>';
//	}
//	var uuid = record.get("uuid");
//	if (! uuid) {
//		return 	'<img style="cursor:pointer" title="'+warningNoSaved+
//		'" onclick=CMDBuild.Administration.MenuPanel.warningNoSaveWindow() src="images/icons/exclamation.png"/>';
//
//	}
//	return '<img style="cursor:pointer" title="'+CMDBuild.Translation.translations+
//			'" onclick=CMDBuild.Administration.MenuPanel.openTranslationsWindow("' + uuid + '") src="images/icons/translate.png"/>';
//}
function htmlComposeMessage(msg) {
	msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, msg);
	return msg;
}


})();
/*TODO put this function in a static class function*/
