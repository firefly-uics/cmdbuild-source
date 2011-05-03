(function() {
	Ext.ns("CMDBuild.administration.class");
	var ns = CMDBuild.administration["class"];

	ns.ModClassController = function(panel, accordion) {
		this.panel = panel;
		this.accordion = accordion;
		this.model = null;
		
		this.panel.on("select", function(model) {
			this.model = model;
		}, this);
		
		this.panel.domainGrid.addAction.on("click", onAddDomainButtonClick, this);
		this.panel.domainGrid.on('rowdblclick', onDomainDoubleClick, this);
	};
	
	function onAddDomainButtonClick() {
		this.onAddDomainButtonClick();
	}
	
	function onDomainDoubleClick(grid, rowIndex, event) {
		var jsonDomain = grid.getStore().getAt(rowIndex).json;
		var idDomain = jsonDomain.idDomain;
		this.onDomainDoubleClick(idDomain);
	}
	
	// implemented on instantiation if needed
	ns.ModClassController.prototype.onAddDomainButtonClick = Ext.emptyFn;
	ns.ModClassController.prototype.onDomainDoubleClick = Ext.emptyFn;
})();