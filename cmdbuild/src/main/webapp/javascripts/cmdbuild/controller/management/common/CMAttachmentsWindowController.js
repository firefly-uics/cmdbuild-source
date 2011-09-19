Ext.define("CMDBuild.controller.management.common.CMAttachmentsWindowController", {

	constructor: function(view) {
		this.view = view;
		this.gridController = new CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController(this.view.grid);

		var cardInfo = this.view.cardInfo;
		this.gridController.updateViewPrivilegesForTypeId(cardInfo.ClassId);
		this.gridController.currentClassId = cardInfo.ClassId;
		this.gridController.currentCardId = cardInfo.Id;
	}
});