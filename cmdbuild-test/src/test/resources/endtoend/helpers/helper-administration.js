TestHelper.prototype = Ext.apply(TestHelper.prototype, {

	/*
	 * UI Access
	 */
	openAccordion: function(accordionName) {
		var accordion = _CMMainViewportController.findAccordionByCMName(accordionName);
		if (accordion) {
			accordion.expand();
		}
	},

	countDomainsInAccordion: function() {
		var accordion = _CMMainViewportController.findAccordionByCMName("domain");
		return accordion.store.tree.root.childNodes.length;
	}
});
