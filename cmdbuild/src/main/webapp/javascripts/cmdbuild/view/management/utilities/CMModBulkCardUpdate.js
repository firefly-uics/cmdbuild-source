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
				text: CMDBuild.Translation.save
			});

			this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.cancel
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

		initComponent: function() {
			Ext.apply(this, {
				tools: [
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Properties', {
						style: {} // Reset margin setup
					})
				]
			});

			this.callParent(arguments);
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
	var CM_INDEX = 'cmIndex',
	TREE_NODE_NAME_ATTRIBUTE = 'cmName',
	TREE_FOLDER_NODE_NAME = 'folder';

	Ext.define('CMDBuild.view.common.CMAccordionStoreModel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: TREE_NODE_NAME_ATTRIBUTE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.TEXT, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.PARENT, type: 'string' },
			{ name: CM_INDEX, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.FILTER, type: 'auto' },
			{ name: 'sourceFunction', type: 'auto' },
			{ name: 'viewType', type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.SECTION_HIERARCHY, type: 'auto' }
		]
	});

	Ext.define('CMDBuild.view.common.CMBaseAccordionStore', {
		extend: 'Ext.data.TreeStore',

		model: 'CMDBuild.view.common.CMAccordionStoreModel',
		root: {
			expanded : true,
			children : []
		},
		sorters: [
			{
				property : CM_INDEX,
				direction: 'ASC'
			},
			{
				property : CMDBuild.ServiceProxy.parameter.TEXT,
				direction: 'ASC'
			}
		]
	});

	/*
	 * this class can not be instantiated,
	 * it is a template for the accordions
	 *
	 * It is a panel with as item a TreePanel,
	 * it may be directly a TreePanel but there are
	 * problemps with the accordion layout
	 * */
	Ext.define('CMDBuild.view.common.CMBaseAccordion', {
		extend: 'Ext.tree.Panel',

		rootVisible: false,

		initComponent: function() {
			this.store = new CMDBuild.view.common.CMBaseAccordionStore();
			this.layout = 'border';
			this.border = true;
			this.autoRender = true;
			this.animCollapse = false;
			this.floatable = false;
			this.bodyStyle = {
				background: '#ffffff'
			};

			this.callParent(arguments);
		},

		updateStore: function(items) {
			var root = this.store.getRootNode(),
				treeStructure = this.buildTreeStructure(items);

			if (Ext.isArray(treeStructure) && treeStructure.length == 0) {
				treeStructure = [{}];
			}

			root.removeAll();
			root.appendChild(treeStructure);
			this.store.sort();
			this.afterUpdateStore();
		},

		selectNodeById: function(node) {
			var sm = this.getSelectionModel();

			if (typeof node != 'object')
				node = this.getNodeById(node);

			if (node) {
				// the expand fail if the accordion is not really
				// visible to the user. But I can not know when
				// a parent of the accordion will be visible, so
				// skip only the expand to avoid the fail
				if (this.isVisible(deep = true))
					node.bubble(function() {
						this.expand();
					});

				sm.select(node);
			} else {
				_debug('I have not found a node with id ' + node);
			}
		},

		selectNodeByIdSilentry: function(nodeId) {
			this.getSelectionModel().suspendEvents();
			this.selectNodeById(nodeId);
			this.getSelectionModel().resumeEvents();
		},

		expandSilently: function() {
			this.cmSilent = true;
			Ext.panel.Panel.prototype.expand.call(this);
		},

		expand: function() {
			this.cmSilent = false;
			this.callParent(arguments);
		},

		removeNodeById: function(nodeId) {
			var node = this.store.getNodeById(nodeId);

			if (node) {
				try {
					node.remove();
				} catch (e) {
					// Rendering issues
				}
			} else {
				_debug('I have not find a node with id ' + nodeId);
			}
		},

		deselect: function() {
			this.getSelectionModel().deselectAll();
		},

		getNodeById: function(id) {
			return this.store.getRootNode().findChild('id', id, deep=true);
		},

		getAncestorsAsArray: function(nodeId) {
			var out = [],
				node = this.store.getRootNode().findChild('id', nodeId, deep=true);

			if (node) {
				out.push(node);

				while (node.parentNode != null) {
					out.push(node.parentNode);
					node = node.parentNode;
				}
			}

			return out;
		},

		isEmpty: function() {
			return !(this.store.getRootNode().hasChildNodes());
		},

		getFirtsSelectableNode: function() {
			if (this.disabled)
				return null;

			var l = this.getRootNode(),
				out = null;

			while (l) {
				if (this.nodeIsSelectable(l)) {
					out = l;
					break;
				} else {
					l = l.firstChild;
				}
			}

			return out;
		},

		nodeIsSelectable: function(node) {
			var name = node.get(TREE_NODE_NAME_ATTRIBUTE),
				isFolder = (!name || name == TREE_FOLDER_NODE_NAME),
				isRootNode = (this.getRootNode() == node),
				isHidden = isRootNode && !this.rootVisible;

			return !isFolder && !isHidden;
		},

		selectFirstSelectableNode: function() {
			var l = this.getFirtsSelectableNode();

			if (l) {
				this.expand();
				// Defer the call because Ext.selection.RowModel
				// for me.views.lenght says 'can not refer to length of undefined'
				Ext.Function.createDelayed(function() {
					this.selectNodeById(l);
				}, 100, this)();
			}
		},

		buildTreeStructure: function() {
			_debug('CMBaseAccordion.buildTreeStructure: buildTreeStructure() unimplemented method');
		},

		afterUpdateStore: function() {}
	});

	Ext.define("CMDBuild.view.common.classes.CMClassAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.classes,
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
			iconCls: node.get("superclass") ? "cmdb-tree-superclass-icon" : "cmdb-tree-class-icon"
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