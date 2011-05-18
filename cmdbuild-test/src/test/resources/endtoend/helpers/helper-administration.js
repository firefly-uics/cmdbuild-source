TestHelper.prototype = Ext.apply(TestHelper.prototype, {

	/*
	 * UI Access
	 */
	openAccordion: function(accordionName) {
		var accordionId = CMDBuild.identifiers.accordion[accordionName];
		var accordion = Ext.getCmp(accordionId);
		if (accordion) {
			accordion.expand();
		}
	},

	countDomainsInAccordion: function() {
		var accordion = Ext.getCmp(CMDBuild.identifiers.accordion.domain);
		return accordion.root.childNodes.length;
	}
	
});
