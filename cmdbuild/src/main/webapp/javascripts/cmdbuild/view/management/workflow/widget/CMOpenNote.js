(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMOpenNotes", {
		extend: "CMDBuild.view.management.classes.CMCardNotesPanel",

		initComponent: function() {
			this.callParent(arguments);
			this.backToActivityButton.hide();
		},

		configure: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activityInstance.raw || c.activityInstance.data;

			var note = this.activity.Notes;
			this.actualForm.setValue(note || "");
			this.displayPanel.setValue(note || "");
		},

		cmActivate: function() {
			this.enable();
			this.backToActivityButton.show();
			this.ownerCt.setActiveTab(this);
			this.enableModify();
		},

		buildButtons: function() {
			this.callParent(arguments);

			this.buttons = this.buttons || [];

			this.backToActivityButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back,
				hidden: true
			});

			this.buttons.push(this.backToActivityButton);
		},

		hideBackButton: function() {
			this.backToActivityButton.hide();
		}
	});
})();