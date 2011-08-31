(function() {
	var tr = CMDBuild.Translation.management.modworkflow;

	Ext.define("CMDBuild.view.management.workflow.CMModProcess", {
		extend: "CMDBuild.view.management.classes.CMModCard",
		cmName: "process",
		
		buildComponents: function() {
			var gridratio = CMDBuild.Config.cmdbuild.grid_card_ratio || 50;

			this.cardGrid = new CMDBuild.view.management.workflow.CMActivityGrid({
				hideMode: "offsets",
				filterCategory: this.cmName,
				border: false,
				columns: []
			});

			this.cardTabPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel({
				region: "south",
				hideMode: "offsets",
				split: true,
				border: false,
				height: gridratio + "%"
			});
		},

		onClassSelected: function(entry) {
			var id = entry.get("id");
			this.cardTabPanel.onClassSelected(id, activateFirst = true);

			this.updateTitleForEntry(entry);
		},

		reset: function(id) {
			this.cardTabPanel.reset(id);
		},

		/*
		o = {
				reloadFields: reloadFields,
				editMode: editMode,
				cb: cb,
				scope: this
			}
		*/
		updateForActivity: function(activity, o) {
			this.cardTabPanel.updateForActivity(activity, o);
		},

		updateForClosedActivity: function(activity) {
			this.cardTabPanel.updateForClosedActivity(activity);
		},

		getWFWidgets: function() {
			return this.cardTabPanel.getWFWidgets();
		},

		onAddButtonClick: function() {
			this.cardTabPanel.onAddButtonClick();
		}
	});
})();