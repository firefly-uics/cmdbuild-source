(function() {
	Ext.define("CMDBuild.view.management.workflow.CMWFWidgetsPanel", {
		extend: "Ext.panel.Panel",

		initComponent: function() {
			Ext.apply(this, {
				layout: "card",
				activeItem: 0,
				hideMode: "offsets",
				border: true,
				frame: false
			});

			this.callParent(arguments);

			this.on("activate", function() {
				this.enable();
			}, this);

			this.on("deactivate", function() {
				this.disable();
			}, this);
		},

		cmActivate: function() {
			this.enable();
			try {
				this.ownerCt.setActiveTab(this);
				this.fireEvent("cmactive");
			} catch (e) {
				Ext.Function.createDelayed(function(){
					this.cmActivate();
				}, 100, this)();
			}
			return this;
		},

		bringToFront: function(widget) {
			this.layout.setActiveItem(widget.id);
		}
	});
})();