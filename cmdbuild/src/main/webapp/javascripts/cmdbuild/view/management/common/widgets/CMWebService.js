(function() {

	Ext.define("CMDBuild.view.management.common.widgets.CMWebServiceItemModel", {
		extend: "Ext.data.Model",
		fields: [{
			name: "text",
			type: "string"
		}, {
			name: "iconCls",
			type: "string"
		}, {
			name: "domNode",
			type: "auto" // An XML DOM Node
		}]
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMWebService", {
		extend: "Ext.panel.Panel",

		initComponent: function() {
			this.frame = false;
			this.border = false;
			this.layout = "border";
			this.autoScroll = true;
			this.callParent(arguments);
		},

		statics : {
			WIDGET_NAME: ".WebService"
		},

		showActionResponse: function(xmlString, selectableNodeName) {
			this.removeAll();

			var store = new Ext.data.TreeStore({
				model: 'CMDBuild.view.management.common.widgets.CMWebServiceItemModel',
				folderSort: true,
				autoLoad: false
			});

			var xmlUtility = CMDBuild.core.xml.XMLUtility;
			var xml = xmlUtility.xmlDOMFromString(xmlString);
			var root = xmlUtility.genericExtTreeFromXMLDom(xml);

			if (selectableNodeName) {
				addCheck(root, selectableNodeName);
			}

			root.expanded = true;
			store.setRootNode(root);

			this.grid = new Ext.tree.Panel({
				region: "center",
				useArrows: true,
				rootVisible: false,
				border: false,
				store: store,
				singleExpand: false,
				hideHeaders: true,
				columns: [{
					xtype: 'treecolumn',
					flex: 1,
					sortable: true,
					dataIndex: 'text'
				}]
			});

			this.add(this.grid);
		},

		getSelectedNodes: function() {
			var selectedNodes = [];
			addSelections(this.grid.getRootNode(), selectedNodes);
			return selectedNodes;
		}
	});

	function addCheck(nodeConfiguration, selectableNodeName) {
		if (nodeConfiguration && selectableNodeName) {
			if (nodeConfiguration.domNode.tagName == selectableNodeName) {
				nodeConfiguration.checked = false;
			}
			if (nodeConfiguration.children) {
				for (var i=0, l=nodeConfiguration.children.length; i<l; ++i) {
					addCheck(nodeConfiguration.children[i], selectableNodeName);
				}
			}
		}
	}

	function addSelections(nodeModel, selectedNodes) {
		if (nodeModel) {
			if (nodeModel.get("checked")) {
				var stringNode = CMDBuild.core.xml.XMLUtility.serializeToString(nodeModel.get("domNode"));
				selectedNodes.push(stringNode);
			}

			if (nodeModel.childNodes) {
				for (var i=0, l=nodeModel.childNodes.length; i<l; ++i) {
					addSelections(nodeModel.childNodes[i], selectedNodes);
				}
			}
		}
	}
	
})();