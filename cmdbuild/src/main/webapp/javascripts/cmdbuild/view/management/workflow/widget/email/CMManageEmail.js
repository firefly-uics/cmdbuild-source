Ext.define("CMDBuild.view.management.workflow.widgets.CMManageEmail", {
	extend: "Ext.panel.Panel",

	loaded: false,

	constructor: function(c) {
		this.widgetConf = c.widget;
		this.activity = c.activity.raw || c.activity.data;
		this.clientForm = c.clientForm;

		this.callParent([this.widgetConf]); // to apply the conf to the panel
	},

	initComponent : function() {
		this.emailGrid = new CMDBuild.Management.EmailGrid( {
			autoScroll : true,
			extAttrDef : this.widgetConf,
			extAttr : this,
			processId : this.activity.Id,
			readWrite : !this.widgetConf.ReadOnly,
			frame: false,
			border: false
		});

		this.backToActivityButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.workflow.back
		});

		Ext.apply(this, {
			frame: false,
			border: false,
			items: [this.emailGrid],
			buttonAlign: "center",
			buttons: [this.backToActivityButton],
			cls: "x-panel-body-default-framed",

			// Wrap the grid
			addTemplateToStore: function(t) {
				return this.emailGrid.addTemplateToStore(t);
			},
			removeTemplatesFromStore: function() {
				return this.emailGrid.removeTemplatesFromStore();
			},
			storeHasNoOutgoing: function() {
				return this.emailGrid.storeHasNoOutgoing();
			},
			CMEVENTS: this.emailGrid.CMEVENTS
		});

		this.callParent(arguments);
		this.addEvents([this.CMEVENTS.updateTemplatesButtonClick]);
		this.relayEvents(this.emailGrid, [this.emailGrid.CMEVENTS.updateTemplatesButtonClick]);
	},

	cmActivate: function() {
		this.mon(this.ownerCt, "cmactive", function() {
			this.ownerCt.bringToFront(this);
		}, this, {single: true});

		this.ownerCt.cmActivate();
	},

	getOutgoing: function(modifiedOnly) {
		var allOutgoing = modifiedOnly ? false : true;
		var outgoingEmails = [];
		var emails = this.emailGrid.getStore().getRange();
		for (var i=0, n=emails.length; i<n; ++i) {
			var currentEmail = emails[i];
			if (allOutgoing || !currentEmail.get("Id") || currentEmail.dirty) {
				outgoingEmails.push(currentEmail.data);
			}
		}
		return outgoingEmails;
	},

	getDeletedEmails: function() {
		return this.emailGrid.deletedEmails;
	}
});