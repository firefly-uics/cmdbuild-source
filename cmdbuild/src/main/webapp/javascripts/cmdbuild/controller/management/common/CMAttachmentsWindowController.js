Ext.define("CMDBuild.controller.management.common.CMAttachmentsWindowController", {

	constructor: function(view) {
		this.view = view;
		this.gridController = new CMDBuild.controller.management.classes.attacchments.CMCardAttacchmentsController(this.view.grid);

		this.gridController.currentClassId = this.view.masterCard.get("IdClass");
		this.gridController.currentCardId = this.view.masterCard.get("Id");
	}
});