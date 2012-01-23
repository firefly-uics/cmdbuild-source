(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMOpenNotes", {
		extend: "CMDBuild.view.management.classes.CMCardNotesPanel",

		initComponent: function() {
			this.callParent(arguments);
			this.backToActivityButton.hide();
		},

		configure: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;
			this.readOnly = c.widget.ReadOnly;

			Ext.apply(this, this.widgetConf);

			var note = this.activity.Notes;
			this.actualForm.setValue(note || "");
			this.displayPanel.setValue(note || "");
		},

		cmActivate: function() {
			this.enable();
			this.backToActivityButton.show();
			this.ownerCt.setActiveTab(this);

			if (this.readOnly) {
				this.disableModify();
			} else {
				this.enableModify();
			}
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