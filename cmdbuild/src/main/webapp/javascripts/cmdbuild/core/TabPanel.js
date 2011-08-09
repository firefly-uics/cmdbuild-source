Ext.define("CMDBuild.TabPanel", {
	extend: "Ext.TabPanel",
	alias: "cmdbtabpanel",

	findItemByAttr: function(attr, val) {
		var items = this.items.items;
		for (var i = 0, l = items.length; i<l; i++) {
			var item = items[i];
			if (item[attr] && item[attr] == val) {
				return item;
			}
		}
		return undefined;
	},
	
	activateTabByAttr: function(attr, val) {
		var tab = this.findItemByAttr(attr, val);
		if (tab) {
			this.activate(tab);
		}
	}
});