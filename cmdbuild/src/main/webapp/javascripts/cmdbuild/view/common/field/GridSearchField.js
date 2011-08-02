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
	}
});