(function() {

	Ext.define("CMDBuild.view.management.utilites.CMModBulkCardUpdate", {
		extend: "Ext.panel.Panel",
		cmName: 'bulkcardupdate',
		title : CMDBuild.Translation.management.modutilities.bulkupdate.title,
		frame: false,
		border: true,

		constructor: function() {
			this.cardSelected = [];

			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save
			});

			this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.abort
			});

			this.classTree = new CMDBuild.view.common.classes.CMClassAccordion({
				cmName: "",
				title: undefined,
				region: "west",
				width: 200,
				border: true,
				split: true,
				excludeSimpleTables: true
			});

			this.cardGrid = new CMDBuild.view.management.common.CMCardGrid({
				region: "center",
				border: true,
				cmAddGraphColumn: false,
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
					idProperty: "Id" // required to identify the records for the data and not the id of ext
				}),
				columns: []
			});

			this.cardForm = new CMDBuild.view.management.utilities.CMBulkCardFormPanel({
				region: "south",
				split: true,
				border: true,
				height: 200
			});

			this.frame = true;
			this.layout = "border";
			this.buttonAlign = "center";
			this.buttons = [this.saveButton,this.abortButton];
			this.items = [{
					xtype: "panel",
					region: "center",
					layout: "border",
					frame: false,
					border: false,
					items: [this.cardGrid, this.cardForm]
				},
				this.classTree
			];

			this.callParent(arguments);
			this.firstShow = true;
		},

		beforeBringToFront : function() {
			if (this.firstShow) {
				this.classTree.updateStore();
				this.firstShow = false;
			}

			return true;
		},

		onClassTreeSelected: function(classId) {
			this.cardForm.fillWithFieldsForClassId(classId);
		},

		saveCardsChanges: function() {
			if (this.cardList.isFiltered()) {

				Ext.Msg.show({
					title : CMDBuild.Translation.warnings.warning_message,
					msg : CMDBuild.Translation.warnings.only_filtered,
					buttons : Ext.Msg.OKCANCEL,
					fn : doSaveRequest,
					icon : Ext.MessageBox.WARNING,
					scope : this
				});

			} else {
				doSaveRequest.call(this, confirm="ok");
			}
		},

		abortCardsChanges: function() {
			this.clearAll();
		},

		clearAll: function() {
			this.cardList.clearFilter();
			this.cardList.getSelectionModel().clearSelections();
			this.cardList.getSelectionModel().clearPersistentSelections();
			this.cardSelected = [];
			this.clearForm();
		},

		clearForm: function() {
			this.attributesPanel.resetForm();
		},

		disableSaveBtnIfSelectionIsEmpty: function() {
			if (this.cardSelected.length < 1){
				this.saveBtn.disable();
			}
		},

		getFilter: function() {
			var filter = {};
			var store = this.cardGrid.getStore();
			if (store
					&& store.proxy
					&& store.proxy.extraParams) {

				filter = store.proxy.extraParams.filter;
			}

			return filter;
		}
	});

	// Here just to avoid refactor of CMModBulkCardUpdate class
	Ext.define("CMDBuild.view.common.classes.CMClassAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modClass.tree_title,
		cmName: "class",
		excludeSimpleTables: false,

		buildTreeStructure: function() {
			var classes = _CMCache.getClasses();
			var standard = []; // the standard CMDBuild classes
			var simpletables = []; // the tables that does not inherit from Class (the root of all evil)
			var nodesMap = {};

			for (var key in classes) {
				var nodeConf =  buildNodeConf(classes[key]);
				nodesMap[nodeConf.id] = nodeConf;
			}

			for (var id in nodesMap) {
				var node = nodesMap[id];
				if (node.tableType == "standard") {
					if (node.parent && nodesMap[node.parent]) {
						linkToParent(node, nodesMap);
					} else {
						standard.push(node);
					}
				} else if (!this.excludeSimpleTables) {
					simpletables.push(node);
				}
			}

			if (simpletables.length == 0) {
				return standard;
			} else {
				return buildFakeRoot(standard, simpletables);
			}
		},

		afterUpdateStore: function() {
			var root = this.store.getRootNode();
			if (root.childNodes.length == 1) {
				this.store.setRootNode(root.getChildAt(0).remove(destroy=false));
			}
		}
	});

	function buildNodeConf(node) {
		return {
			id: node.get("id"),
			text: node.get("text") != "Class" ? node.get("text") : CMDBuild.Translation.administration.modClass.classProperties.standard,
			name: node.get("name"),
			tableType: node.get("tableType"),
			leaf: true,
			cmName: node.get("text") != "Class" ? "class" : "",
			parent: node.get("parent"),
			cmData: node.data,
			iconCls: node.get("superclass") ? "cmdbuild-tree-superclass-icon" : "cmdbuild-tree-class-icon"
		};
	}

	function linkToParent(node, nodesMap) {
		var parentNode = nodesMap[node.parent];
		parentNode.children = (parentNode.children || []);
		parentNode.children.push(node);
		parentNode.leaf = false;
	}

	function buildFakeRoot(standard, simpletables) {
		var first = standard[0];
		first.cmIndex = 1;
		first.expanded = true;

		return {
			leaf: false,
			children:[
				first,
				{
					text: CMDBuild.Translation.administration.modClass.classProperties.simple,
					leaf: false,
					children: simpletables,
					cmIndex: 2,
					expanded: true
				}
			]
		}
	}
})();