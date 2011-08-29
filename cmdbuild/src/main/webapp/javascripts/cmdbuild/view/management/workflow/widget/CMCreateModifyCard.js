(function() {
	// TODO 
	// manage reference: if there is a reference, update the value of the combo after card save
	// remove old code

	Ext.define("CMDBuild.view.management.workflow.widgets.CMCreateModifyCard", {
		extend: "CMDBuild.view.management.classes.CMCardPanel",
		withButtons: true,
		constructor: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;
			this.noSelect = c.noSelect;
	
			this.callParent([this.widgetConf]); // to apply the conf to the panel
		},

		buildTBar: function() {
			this.cmTBar = [
				this.addCardButton = new CMDBuild.AddCardMenuButton({
					classId: undefined
				})
			];
		},

		cmActivate: function() {
			this.mon(this.ownerCt, "cmactive", function() {
				this.ownerCt.bringToFront(this);
			}, this, {single: true});

			this.ownerCt.cmActivate();
		},

		initWidget: function(idClass, cardId) {
			var et = _CMCache.getEntryTypeById(idClass);

			if (et.data.superclass) {
				this.addCardButton.updateForEntry(et);
				getTopToolbar.call(this).show();
			} else {
				getTopToolbar.call(this).hide();
				if (!cardId) {
					this.onAddCardButtonClick(this.idClass, reloadFields = true);
				} else {
					// fake obj to use the onCardSelected and force the
					// loading of remote data
					var card = {
						get: function(k) {
							var data = {
								IdClass: idClass,
								Id: cardId
							}

							return data[k];
						}
					};

					this.forceEditMode = !this.widgetConf.ReadOnly; // see fillForm of CMCardPanel
					this.onCardSelected(card, reloadField = true, loadRemoteData = true);
				}
			}
		},

		// override
		buildButtons: function() {
			this.callParent(arguments);
			this.cmButtons = this.cmButtons || [];

			this.backToActivityButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back
			});

			this.cmButtons.push(this.backToActivityButton);
		}
	});

	function getTopToolbar() {
		return this.addCardButton.ownerCt;
	}
})();