(function() {

	var FILTER = "FILTER";
	var SQL = "SQL";
	var DATA_VIEW = "dataView";

	Ext.define("CMDBuild.view.management.dataView.CMDataViewAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Data View",

		cmName: DATA_VIEW,

		excludeSimpleTables: false,

		// override
		buildTreeStructure: function(items) {
			var children = [];

			for (var i=0, l=items.length; i<l; ++i) {
				var viewConfiguration = items[i];
				children.push(buildNodeConf(viewConfiguration));
			}

			return children;
		},

		// override
		afterUpdateStore: function() {}
	});

	function buildNodeConf(viewConfiguration) {
		var node = {
			text: viewConfiguration.description,
			tableType: "standard",
			leaf: true,
			iconCls: "cmdbuild-tree-class-icon"
		};

		if (viewConfiguration.type == FILTER) {
			node.viewType = FILTER;

			var entryTypeName = viewConfiguration.sourceClassName;
			var entryType = _CMCache.getEntryTypeByName(entryTypeName);

			node.id = entryType.getId();
			node.filter = viewConfiguration.filter;
			node.cmName = "class"; // To act as a regular class node
		} else {
			node.viewType = SQL;
			node.sourceFunction = viewConfiguration.sourceFunction;
			node.cmName = DATA_VIEW;
		}

		return node;
	}

})();