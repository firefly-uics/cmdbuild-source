Ext.define("CMDBuild.field.CMToggleButtonToShowReferenceAttributes", {
	extend: "Ext.button.Button",
	subfieldsPanel: undefined, // passed on instantiation
	enableToggle: true,
	iconCls: "down",
	cls: "clearButtonBGandBorder",
	listeners: {
		toggle: function(b, pressed) {
			if (pressed) {
				b.subfieldsPanel.show();
				b.setIconCls("up-hover");
			} else {
				b.subfieldsPanel.hide();
				b.setIconCls("down-hover");
			}
		},
		mouseover: function(b) {
			if (b.iconCls == "down") {
				b.setIconCls("down-hover");
			} else {
				b.setIconCls("up-hover");
			}
		},
		mouseout: function(b) {
			if (b.iconCls == "down-hover") {
				b.setIconCls("down");
			} else {
				b.setIconCls("up");
			}
		}
	}
});