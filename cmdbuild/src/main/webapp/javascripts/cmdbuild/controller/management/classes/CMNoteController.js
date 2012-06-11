(function() {
	Ext.define("CMDBuild.controller.management.classes.CMNoteController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",
		constructor: function(view, supercontroller) {

			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.CMEVENTS = {
				noteWasSaved: "cm-note-saved"
			};

			this.mon(this.view, this.view.CMEVENTS.saveNoteButtonClick, this.onSaveNoteClick, this);
			this.mon(this.view, this.view.CMEVENTS.cancelNoteButtonClick, this.onCancelNoteClick, this);

			this.addEvents(this.addEvents.noteWasSaved);
		},

		onEntryTypeSelected: function() {
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);
			this.updateView(card);

			if (this.disableTheTabBeforeCardSelection(card)) {
				this.view.disable();
			} else {
				this.view.enable();
				this.view.loadCard(card);
			}
		},

		disableTheTabBeforeCardSelection: function(card) {
			return !card || CMDBuild.Utils.isSimpleTable(card.get("IdClass"));
		},

		updateView: function(card) {
			this.updateViewPrivilegesForCard(card);
			this.view.reset();
			this.view.disableModify();
		},

		updateViewPrivilegesForCard: function(card) {
			var priv = false;
			if (card && card.raw) {
				priv = card.raw.priv_write;
			}

			this.view.updateWritePrivileges(priv);
		},

		onSaveNoteClick: function() {
			var me = this,
				form = me.view.getForm(),
				params = {
					IdClass: me.card.get("IdClass"),
					Id: me.card.get("Id")
				};

			if (form.isValid() && me.beforeSave(me.card)) {
				CMDBuild.LoadMask.get().show();
				form.submit({
					method : 'POST',
					url : 'services/json/management/modcard/updatecard',
					params: params,
					success : function() {
						CMDBuild.LoadMask.get().hide();
						me.view.disableModify(enableToolbar = true);
						var val = me.view.syncForms();
						syncSavedNoteWithModel(me.card, val);
						me.fireEvent(me.CMEVENTS.noteWasSaved);
					},
					failure: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},

		onCancelNoteClick: function() {
			this.view.loadCard(this.card);
			this.view.disableModify(couldModify = this.card.raw.priv_write);
		},

		// called before the save request
		// override in subclass, return false to avoid the save
		beforeSave: function(card) {
			return true;
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