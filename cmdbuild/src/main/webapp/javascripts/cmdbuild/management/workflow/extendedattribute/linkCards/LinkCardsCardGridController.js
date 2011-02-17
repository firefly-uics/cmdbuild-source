CMDBuild.Management.LinkCards.LinkCardsCardGridController = function(grid, model) {
	this.model = model;
	this.grid = grid;
	
	grid.controller = this;
	grid.model = model;
	
	grid.getSelectionModel().on('rowdeselect', function(sm, rowIndex, record) {
		this.model.deselect(record.get("Id"));
	}, this);
	
	grid.getSelectionModel().on('rowselect', function(sm, rowIndex, record) {
		this.model.select(record.get("Id"));
	}, this);
	
	grid.on("CM_load", function() {
		grid.syncSelections();
	}, this);
	
	model.on("select", function(selection) {
		this.grid.selectByCardId(selection);
	}, this);
	
	model.on("deselect", function(selection) {
		this.grid.deselectByCardId(selection);
	}, this);
};

CMDBuild.Management.LinkCards.LinkCardsCardGridController.prototype.loadPageForLastSelection = function(selection) {
	if (selection != null) {
		this.grid.loadPageForCardId(selection);
	} else {
		this.grid.getStore().reload();
	}
};