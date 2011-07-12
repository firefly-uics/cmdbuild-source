(function() {
	Ext.define("CMDBuild.controller.accordion.CMDomainAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);
			
			_CMCache.on("cm_domain_saved", updateStore, this);
			_CMCache.on("cm_domain_deleted", onDomainDeleted, this);
		}
	});

	function updateStore(domain) {
		this.accordion.updateStore();
		this.accordion.selectNodeById(domain.get("id"));
	}
	
	function onDomainDeleted(id) {
		this.accordion.removeNodeById(id);
		this.accordionSM.deselectAll();
	}
})();