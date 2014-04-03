(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMNavigationTreesAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ NavigationTrees",
		cmName: "navigationTrees",

		initComponent: function() {
			this.callParent(arguments);
			_CMCache.registerOnNavigationTrees(this);
		},
		
		buildTreeStructure: function() {
			var navigationTrees = _CMCache.getNavigationTrees();
			var out = [];

			for (var key in navigationTrees.data) {
				out.push(buildNodeConf(navigationTrees.data[key]));
			}

			return out;
		},
		
		refresh: function() {
			var navigationTrees = _CMCache.getNavigationTrees();
			this.updateStore(navigationTrees.data);
			if (navigationTrees.lastEntry) {
				this.selectNodeById(navigationTrees.lastEntry);
			}
			else {
				this.selectFirstSelectableNode();
			}
		}
	});

	function buildNodeConf(d) {
		return {
			id: d,
			text: d,
			leaf: true,
			cmName: "navigationTrees",
			iconCls: "navigationTrees"
		};
	}

})();