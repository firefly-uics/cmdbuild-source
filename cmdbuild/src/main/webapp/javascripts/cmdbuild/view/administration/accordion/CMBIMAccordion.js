(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMBIMAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: "@@ BIM",

		cmName: "bim",

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();
			root.appendChild([{
				text: "@@ Projects",
				leaf: true,
				cmName: "bim-project"
			}]);
		}
	});

})();