(function() {

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		constructor: function(v, owner) {
			this.view = v;
			this.ownerController = owner;
			// this flag is used to define if the user has click on the
			// save or advance button. The difference is that on save 
			// the widgets do nothing and the saved activity goes in display mode.
			// On advance, otherwise, the widgets do the react (save their state) and
			// the saved activity lies in edit mode, to continue the data entry.
			this.isAdvance = false;

			// listeners
			this.view.activityForm.saveButton.on("click", onSaveButtonClick, this);
			this.view.activityForm.advanceButton.on("click", onAdvanceButtonClick, this);
			this.view.activityForm.cancelButton.on("click", onAbortButtonClick, this);

			this.view.activityForm.deleteCardButton.on("click", onDeleteButtonClick, this);
			this.view.activityForm.modifyCardButton.on("click", onModifyButtonClick, this);

			this.view.wfWidgetsPanel.on("cm-wfwidgetbutton-click", onWFWidgetButtonClick, this);
		},

		onEntrySelected: function(selection) {
			this.view.displayMode();
			this.view.reset();
		},

		clearViewForNoActivity: function() {
			this.view.clearForNoActivity();
		},

		onActivitySelect: function(activity, reloadFields, editMode) {
			this.isAdvance = false;
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
	
	function onWFWidgetButtonClick(wfWidgetData) {
		this.ownerController.onWFWidgetButtonClick(wfWidgetData);
	}
})();