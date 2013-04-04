Ext.define("CMDBuild.field.GridSearchField", {

	extend: "Ext.form.field.Trigger",
	trigger1Cls: Ext.baseCSSPrefix + 'form-search-trigger',
	trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
	validationEvent:false,
	validateOnBlur:false,
	hideTrigger1 :false,
	hideTrigger2 :false,
	
	initComponent : function(){
		this.callParent(arguments);

		this.on('specialkey', function(f, e){
			if(e.getKey() == e.ENTER){
				this.onTrigger1Click();
			}
		}, this);
	},

	onTrigger1Click : function() {
		var s = this.grid.getStore();
		s.proxy.extraParams["query"] = this.getRawValue();
		s.loadPage(1);
	},

	onTrigger2Click: function(e){
		if (!this.disabled) {
			this.setValue("");
			this.onTrigger1Click();
		}
	},

	reset: function() {
		var s = this.grid.getStore();
		this.setValue("");
		s.proxy.extraParams["query"] = this.getRawValue();
	}
});

Ext.define("CMDBuild.field.LocalGridSearchField", {
	extend: "CMDBuild.field.GridSearchField",

	// configuration
	grid: null,
	// configuration

	/**
	 * Filter the loaded record
	 * comparing every data element
	 * with the content of the field
	 * (case insensitive) 
	 */
	onTrigger1Click: function() {
		var query = this.getValue() || "";
		var s = this.grid.getStore();
		s.clearFilter();

		s.filter({
			filterFn: function(item) {
				var data = item.data;

				for (var key in data) {
					var value = data[key] || "";
					if ((""+value).toUpperCase()
							.indexOf(query.toUpperCase()) !== -1) {

						return true;
					}
				}

				return false;
			}
		});
	},

	onTrigger2Click: function() {
		var s = this.grid.getStore();
		s.clearFilter();
		this.setValue("");
	},

	reset: function() {
		this.onTrigger2Click();
	}
});