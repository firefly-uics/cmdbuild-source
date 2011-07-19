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

		onEntrySelect: function(selection) {
			
		},

		onActivitySelect: function(activity, reloadFields) {
			this.view.loadActivity(activity, reloadFields);
		},
		
		// p = {activity: the activity, edit: boolean, isNew: boolean}
		onAddButtonClick: function(p) {
			this.view.loadActivity(p.activity, reloadFields = true);
		}
	});

	function onSaveButtonClick() {
		this.isAdvance = false;
		this.ownerController.onSaveButtonClick();
	}

	function onAdvanceButtonClick() {
		this.isAdvance = true;
		alert("@@ on advance button click");
	}

	function onAbortButtonClick() {
		alert("@@ on delete button click");
	}
	
	function onDeleteButtonClick() {
		this.ownerController.onDeleteButtonClick();
	}
	
	function onModifyButtonClick() {
		alert("@@ on modify button click");
	}
})();