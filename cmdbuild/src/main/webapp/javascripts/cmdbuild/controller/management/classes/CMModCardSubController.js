Ext.define("CMDBuild.controller.management.classes.CMModCardSubController", {
	mixins : {
		observable : "Ext.util.Observable"
	},

	constructor : function(view, supercontroller) {
		if (typeof view == "undefined") {
			throw ("OOO snap, you have not passed a view to me");
		} else {
			this.view = view;
		}

		this.superController = supercontroller;
		this.card = null;
		this.entryType = null;
	},

	onEntryTypeSelected : function(entryType) {
		this.entryType = entryType;
	},

	onCardSelected : function(card) {
		this.card = card;
	},

	onAddCardButtonClick : function(classIdOfNewCard) {

	},

	onShowGraphClick: function() {
		var classId = this.card.get("IdClass"),
			cardId = this.card.get("Id");

		CMDBuild.Management.showGraphWindow(classId, cardId);
	}
});