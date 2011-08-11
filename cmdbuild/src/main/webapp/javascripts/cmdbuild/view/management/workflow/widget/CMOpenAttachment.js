(function() {
	Ext.define("CMDBuild.view.management.workflow.widgets.CMOpenAttachment", {
		extend: "CMDBuild.view.management.classes.attacchments.CMCardAttachmentsPanel",

		initComponent: function() {
			this.callParent(arguments);
			this.mon(this, "activate", function() {
				this.enable();
			}, this);
	
			this.mon(this, "deactivate", function() {
				this.disable();
			}, this);
		},

		configure: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;
			this.noSelect = c.noSelect;

			Ext.apply(this, this.widgetConf);

			this.setExtraParams({
				IdClass: this.activity.IdClass,
				Id: this.activity.Id
			});

			this.loaded = false;
		},

		cmActivate: function() {
			this.ownerCt.setActiveTab(this);
		}
	});
})();