
(function() {
	Ext.define("CMDBuild.controller.management.workflow.CMNoteController", {
		extend: "CMDBuild.controller.management.classes.CMNoteController",

		// override to deny to add a note to a new process
		disableTheTabBeforeCardSelection: function(card) {
			if (isANewActivity(card)) {
				return true;
			} else {
				return this.callParent(arguments);
			}
		},

		// override: return alwais false because we want that
		// in process the user could modify the notes only if
		// there is an openNote extended attribute defined.
		updateViewPrivilegesForCard: function(card) {
			this.view.updateWritePrivileges(false);
		},

		// is not possible to save the note if the
		// activity is not already saved
		beforeSave: function(card) {
			var isNew = isANewActivity(card);

			if (isNew) {
				new CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
					CMDBuild.Translation.management.modworkflow.extattrs.notes.must_save_to_modify,
					popup = false);
			}

			return !isNew;
		}
	});

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMOpenNoteController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",

		constructor: function(view, ownerController, widget, card) {
			this.callParent(arguments);

			this.card = card;
			try {
				this.view.updateWritePrivileges(this.card.raw.priv_write && !this.readOnly);
			} catch (e) {
				this.view.updateWritePrivileges(false);
			}

			this.view.disableModify();

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.callParent(arguments);
			this.mun(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		onBackToActivityButtonClick: function() {
			try {
				this.view.hideBackButton();
				this.view.disableModify();
				this.ownerController.showActivityPanel();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		}
	});

	function isANewActivity(a) {
		return (typeof a.get == "function" && a.get("Id") == -1);
	}

	function syncSavedNoteWithModel(activity, val) {
		activity.set("Notes", val);
		activity.commit();
		if (activity.raw) {
			activity.raw["Notes"] = val;
		}
	}
})();