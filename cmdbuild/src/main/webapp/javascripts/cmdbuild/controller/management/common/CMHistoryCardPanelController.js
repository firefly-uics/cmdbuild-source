(function() {
	Ext.define("CMDBuild.controller.management.classes.CMCardHistoryPanelController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			if (card) {
				if (this.entryType.get("tableType") != CMDBuild.Constants.cachedTableType.simpletable) {
					var existingCard = (!!this.card);
					this.view.setDisabled(!existingCard);

					if (this.view.tabIsActive(this.view)) {
						this.load();
					} else {
						this.mon(this.view, "activate", this.load, this, {single: true});
					}
				} else {
					this.view.disable();
				}
			}
		},

		onAddCardButtonClick: function() {
			this.view.disable();
		},

		load: function() {
			var me = this;
			this.view.getStore().load({
				params : {
					IdClass: me.card.get("IdClass"),
					Id: me.card.get("Id")
				}
			});
		}
	});

	Ext.define("CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController", {
		extend: "CMDBuild.controller.management.classes.CMCardHistoryPanelController",
		onCardSelected: function(card) {
			if (card._cmNew) {
				this.card = card;
				this.view.disable();
			} else {
				this.callParent(arguments);
			}
		}
	});
})();