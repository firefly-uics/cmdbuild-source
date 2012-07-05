
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

		// override: return always false because we want that
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
		},

		// override to retrieve the activityInstance
		// from the WorkflowState
		_getSaveParams: function() {
			var ai = _CMWFState.getActivityInstance();
			var processClass = _CMWFState.getProcessClassRef();

			if (ai && processClass) { // FIXME the process class id must be taken from ai
				return {
					IdClass: processClass.getId(),
					Id: ai.getId()
				};
			} else {
				return {};
			}
		}
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMOpenNoteController", {
		extend: "CMDBuild.controller.management.common.widgets.CMWidgetController",

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: ".OpenNote"
		},

		constructor: function(view, supercontroller, widget, templateResolver, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			try {
				this.view.updateWritePrivileges(this.card.raw.priv_write && !this.readOnly);
			} catch (e) {
				this.view.updateWritePrivileges(false);
			}

			this.view.disableModify();

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.mun(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		onBackToActivityButtonClick: function() {
			try {
				this.view.hideBackButton();
				this.view.disableModify();
				this.ownerController.activateFirstTab();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		}
	});

	function isANewActivity() {
		var ai = _CMWFState.getActivityInstance();

		if (ai) {
			return  ai.isNew();
		} else {
			return false;
		}
	}

	function syncSavedNoteWithModel(activity, val) {
		activity.set("Notes", val);
		activity.commit();
		if (activity.raw) {
			activity.raw["Notes"] = val;
		}
	}
})();