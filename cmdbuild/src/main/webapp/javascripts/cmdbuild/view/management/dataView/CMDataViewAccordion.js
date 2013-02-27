(function() {

	Ext.define("CMDBuild.view.management.dataView.CMDataViewAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ Data View",

		cmName: "dataView",

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
		var entryTypeName = viewConfiguration.sourceClassName;
		var entryType = _CMCache.getEntryTypeByName(entryTypeName);

		return {
			id: entryType.getId(),
			text: viewConfiguration.description,
			tableType: "standard",
			leaf: true,
			cmName: "class",
			iconCls: "cmdbuild-tree-class-icon",
			filter: viewConfiguration.filter
		};
	}

})();