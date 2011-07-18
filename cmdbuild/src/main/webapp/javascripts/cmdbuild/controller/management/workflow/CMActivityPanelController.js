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
		},

		onEntrySelect: function(selection) {
			
		},

		onActivitySelect: function(activity, reloadFields) {
			this.view.loadActivity(activity, reloadFields);
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
		alert("@@ on abort button click");
	}
})();