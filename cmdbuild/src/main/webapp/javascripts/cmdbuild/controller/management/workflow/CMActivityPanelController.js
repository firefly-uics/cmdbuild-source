(function() {

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		constructor: function(v, owner) {
			this.view = v;
			this.ownerController = owner;
			this.isAdvance = false;

			// listeners
			this.view.activityForm.saveButton.on("click", onSaveButtonClick, this);
			this.view.activityForm.advanceButton.on("click", onAdvanceButtonClick, this);
			this.view.activityForm.cancelButton.on("click", onAbortButtonClick, this);

			this.view.activityForm.deleteCardButton.on("click", onDeleteButtonClick, this);
			this.view.activityForm.modifyCardButton.on("click", onModifyButtonClick, this);
		},

		onEntrySelected: function(selection) {
			this.view.displayMode();
			this.view.reset();
		},

		onActivitySelect: function(activity, reloadFields, editMode) {
			this.view.loadActivity(activity, reloadFields, editMode);
			this.isAdvance = false;
		},

		// p = {activity: the activity, edit: boolean, isNew: boolean}
		onAddButtonClick: function(p) {
			this.view.loadActivity(p.activity, reloadFields = true, editMode = true);
		}
	});

	function onSaveButtonClick() {
		this.isAdvance = false;
		this.ownerController.onSaveButtonClick();
	}

	function onAdvanceButtonClick() {
		this.isAdvance = true;
		this.ownerController.onSaveButtonClick();
	}

	function onAbortButtonClick() {
		this.isAdvance = false;
		this.ownerController.onAbortButtonClick();
	}

	function onDeleteButtonClick() {
		this.isAdvance = false;
		this.ownerController.onDeleteButtonClick();
	}

	function onModifyButtonClick() {
		this.isAdvance = false;
		this.view.editMode();
	}
})();