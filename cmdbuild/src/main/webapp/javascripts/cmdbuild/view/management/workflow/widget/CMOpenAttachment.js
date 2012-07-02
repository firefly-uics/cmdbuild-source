(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMOpenAttachment", {
		extend: "CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel",

		initComponent: function() {
			this.backToActivityButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back
			});

			Ext.apply(this, {
				buttonAlign: "center",
				buttons: [this.backToActivityButton],
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);
		},

		configure: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activityInstance.raw || c.activityInstance.data;
			this.clientForm = c.clientForm;
			this.readOnly = c.widget.ReadOnly;

			Ext.apply(this, this.widgetConf);

			this.setExtraParams({
				IdClass: this.activity.IdClass,
				Id: this.activity.Id
			});

			this.writePrivileges = this.activity.priv_write && !this.readOnly;
			this.addAttachmentButton.setDisabled(!this.writePrivileges);

			this.loaded = false;
		},

		cmActivate: function() {
			this.enable();

			// rendering issues, call showBackButton only after	
			// that the  panel did actually activated
			this.mon(this, "activate", this.showBackButton, this, {single: true});

			this.ownerCt.setActiveTab(this);
		},

		hideBackButton: function() {
			this.backToActivityButton.hide();
		},

		showBackButton: function() {
			this.backToActivityButton.show();
			this.ownerCt.doLayout();
		}
	});
})();