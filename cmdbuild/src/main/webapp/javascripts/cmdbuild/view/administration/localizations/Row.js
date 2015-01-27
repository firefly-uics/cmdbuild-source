(function() {

	Ext.define("CMDBuild.view.administration.localizations.Row", {
		extend: "Ext.container.Container",
		layout: "hbox",
		padding: "0 0 0 5",
		field1: undefined,
		field2: undefined,
		field3: undefined,
		initComponent: function() {
			this.items = [];
			if (this.field1) {
				this.items.push(this.field1);
			}
			if (this.field2) {
				this.items.push(this.field2);
			}
			if (this.field3) {
				this.items.push(this.field3);
			}
			this.callParent(arguments);
		}
	});

})();