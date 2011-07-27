(function() {

Ext.define("CMDBuild.AddRelationMenuButton", {
	extend: "Ext.button.Split",
	translation: "@@ Add relation",
	iconCls: 'add',

	//custom fields
	baseText: "@@ Add relation",
	textPrefix: "@@ Add relation",
	
	//private
	initComponent: function() {
		Ext.apply(this, {
			text: this.baseText,
			menu : {items :[]},
			handler: onClick,
			scope: this
		});

		this.callParent(arguments);
	},

	setDomainsForEntryType: function(et) {
		if (!et) {
			return;
		}

		this.menu.removeAll();
		var d,
			domains = _CMCache.getDirectedDomainsByEntryType(et);

		for (var i=0, l=domains.length; i<l; ++i) {
			d = domains[i];
			this.menu.add({
				text: d.description,
				domain: d,
				scope: this,
				handler: function(item, e){
					this.fireEvent("cmClick", item.domain);
				}
			});
		}
		
		return domains.length > 0;
	},

	
	setTextSuffix: function(suffix) {
		this.setText(this.textPrefix +" "+suffix);
	},
	
	//private
	isEmpty: function(){
		return (this.menu.items.items.length == 0 );
	},
	
	//private
	resetText: function() {
		this.setText(this.baseText);
	}

});

	function onClick() {
		//Extjs calls the handler even when disabled
		if (!this.disabled) {
			this.showMenu();
		}
	}

})();