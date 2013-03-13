(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMFilterAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.management.findfilter.set_filter,

		hideMode: "offsets",

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();

			root.appendChild({
				text: "@@ Filtri di gruppo",
				cmName: "groupfilter",
				leaf: true
			});
		}
	});

})();