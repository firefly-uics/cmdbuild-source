(function() {
	Ext.define("CMDBuild.controller.management.classes.CMNoteController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",
		constructor: function(view, supercontroller) {
			this.callParent(arguments);

			this.mon(this.view, this.view.CMEVENTS.saveNoteButtonClick, this.onSaveNoteClick, this);
			this.mon(this.view, this.view.CMEVENTS.cancelNoteButtonClick, this.onCancelNoteClick, this);
		},

		onEntryTypeSelected: function() {
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			if (!card || CMDBuild.Utils.isSimpleTable(card.get("IdClass"))) {
				this.view.disable();
				return;
			} else {
				this.view.enable();
				this.view.reset();
				this.view.loadCard(card);
				this.view.disableModify(couldModoify = card.raw.priv_write);
			}
		},

		onSaveNoteClick: function() {
			var me = this,
				form = me.view.getForm(),
				params = {
					IdClass: me.card.get("IdClass"),
					Id: me.card.get("Id")
				};

			if (form.isValid()) {
				CMDBuild.LoadMask.get().show();
				form.submit({
					method : 'POST',
					url : 'services/json/management/modcard/updatecard',
					params: params,
					success : function() {
						CMDBuild.LoadMask.get().hide();
						me.view.disableModify();
						var val = me.view.syncForms();
						syncSavedNoteWithModel(me.card, val);
					},
					failure: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},

		onCancelNoteClick: function() {
			this.view.loadCard(this.card);
			this.view.disableModify(couldModoify = this.card.raw.priv_write);
		}
	});

	function syncSavedNoteWithModel(card, val) {
		card.set("Notes", val);
		card.commit();
		if (card.raw) {
			card.raw["Notes"] = val;
		}
	}

	Ext.define("CMDBuild.view.management.common.CMNoteWindowController", {
		extend: "CMDBuild.controller.management.classes.CMNoteController",
		constructor: function() {
			this.callParent(arguments);
		},

		onCardSelected: function(card) {
			this.callParent(arguments);
			if (this.card) {
			var title = Ext.String.format("{0} - {1}"
					, CMDBuild.Translation.management.modcard.tabs.notes 
					, this.card.get("Description"));
			}

			this.view.setTitle(title);
		}
	});
})();