(function() {
	Ext.define("CMDBuild.view.management.workflow.CMWFWidgetsPanel", {
		extend: "CMDBuild.PopupWindow",

		initComponent: function() {
			this.widgetsContainer = new Ext.panel.Panel({
				layout: "card",
				activeItem: 0,
				hideMode: "offsets",
				border: false,
				frame: false
			});

			var me = this;
			Ext.apply(this, {
				items: [this.widgetsContainer],
				buttonAlign: "center",
				buttons: [{
					text: CMDBuild.Translation.common.buttons.workflow.back,
					handler: function() {
						me.hide();
					}
				}]
			});

			this.callParent(arguments);
		},

		showWidget: function(widget) {
			this.show();
			this.widgetsContainer.layout.setActiveItem(widget.id);
		},

		addWidgt: function(w) {
			this.widgetsContainer.add(w);
		},

		destroy: function() {
			this.widgetsContainer.removeAll(autodestroy = true);
		}
	});
})();