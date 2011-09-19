Ext.define("CMDBuild.view.management.common.CMAttachmentsWindow", {
	extend: "CMDBuild.PopupWindow",
	cardInfo: null, // { Description, Id, ClassId
	initComponent: function() {
		this.title = Ext.String.format("{0} - {1}"
				, CMDBuild.Translation.management.modcard.tabs.attachments 
				, this.cardInfo.Description);

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
				IdClass: this.cardInfo.ClassId,
				Id: this.cardInfo.Id
			});

			this.grid.getStore().load();
		}, this);
	}
});