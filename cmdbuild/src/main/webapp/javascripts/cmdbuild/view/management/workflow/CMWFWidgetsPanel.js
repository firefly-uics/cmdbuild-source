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
					_cmNotRemoveMe: true, // flag to identiry the button when clean the buttons bar
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
			this.removeExtraButtons();
			if (widget.getExtraButtons) {
				var extraButtons = widget.getExtraButtons();
				this.addExtraButtons(extraButtons);
			}
		},

		addWidgt: function(w) {
			this.widgetsContainer.add(w);
		},

		destroy: function() {
			this.widgetsContainer.removeAll(autodestroy = true);
		},

		addExtraButtons: function(extraButtons) {
			var bar = this.getButtonBar();
			if (bar) {
				bar.add(extraButtons);
			}
		},

		removeExtraButtons: function() {
			var bar = this.getButtonBar();
			if (bar) {
				bar.items.each(function(i) {
					if (i._cmNotRemoveMe) {
						return;
					} else {
						bar.remove(i);
					}
				})
			}
		},

		// I have not found a clean solution to have the buttons bar,
		// generated in a panel with the "buttons" configuration object
		getButtonBar: function() {
			var docks = this.getDockedItems();
			for (var i=0, l=docks.length; i<l; ++i) {
				var d = docks[i];
				if (d.dock == "bottom") {
					return d;
				}
			}
			return null;
		}
	});
})();