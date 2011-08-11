(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMOpenNotes", {
		extend: "CMDBuild.view.management.classes.CMCardNotesPanel",

		initComponent: function() {
			this.callParent(arguments);
			this.on("activate", function() {
				this.enable();
			}, this);
	
			this.on("deactivate", function() {
				this.disable();
			}, this);
		},

		configure: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;
			this.noSelect = c.noSelect;

			Ext.apply(this, this.widgetConf);

			var note = this.activity.Notes;
			this.actualForm.setValue(note || "");
			this.displayPanel.setValue(note || "");
		},

		cmActivate: function() {
			this.ownerCt.setActiveTab(this);
			this.disableModify();
		}
	});
})();