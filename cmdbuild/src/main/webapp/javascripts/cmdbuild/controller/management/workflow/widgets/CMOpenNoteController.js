(function() {
	Ext.define("CMDBuild.controller.management.workflow.widgets.CMOpenNoteController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		
		constructor: function() {
			this.callParent(arguments);

			this.view.mon(this.view.saveButton, "click", this.onSaveNoteClick, this);
		},

		onSaveNoteClick: function() {
			var activity = this.ownerController.currentActivity,
				form = this.view.actualForm.getForm();
			
			if (typeof activity.get == "function") {
				var params = {
					IdClass: activity.get("IdClass"),
					Id: activity.get("Id")
				};
				if (form.isValid()) {
					form.submit({
						method : 'POST',
						url : 'services/json/management/modcard/updatecard',
						waitTitle : CMDBuild.Translation.common.wait_title,
						waitMsg : CMDBuild.Translation.common.wait_msg,
						scope: this,
						params: params,
						success : function() {
							this.view.disableModify();
							var val = this.view.actualForm.getValue();
							this.view.displayPanel.setValue(val);
							syncActivityWithNewValue(activity, val);
						}
					});
				}
			} else {
				// TODO alert that is not possible to 
				// save notes at first step 
			}
		}
	});

	function syncActivityWithNewValue(activity, val) {
		activity.set("Notes", val);
		activity.commit();
		if (activity.raw) {
			activity.raw["Notes"] = val;
		}
	}
})();