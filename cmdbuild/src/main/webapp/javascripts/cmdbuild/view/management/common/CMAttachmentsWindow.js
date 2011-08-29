Ext.define("CMDBuild.view.management.common.CMAttachmentsWindow", {
	extend: "CMDBuild.PopupWindow",
	masterCard: null,
	initComponent: function() {
		this.title = Ext.String.format("{0} - {1}"
				, CMDBuild.Translation.management.modcard.tabs.attachments 
				, this.masterCard.Description);

		this.grid = new CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel({
			border: false
		});

		var closeButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.close,
			name: 'saveButton',
			formBind: true,
			handler: function() {
				this.destroy();
			},
			scope: this
		});

		Ext.apply(this, {
			items : [ this.grid ],
			buttons : [ closeButton ],
			buttonAlign : "center"
		});

		this.callParent(arguments);

		this.on("show", function() {
			this.grid.setExtraParams({
				IdClass: this.masterCard.get("IdClass"),
				Id: this.masterCard.get("Id")
			});

			this.grid.getStore().load();
		}, this);
	}
});