CMDBuild.CMDBuildCombo = Ext.extend(Ext.ux.form.XComboBox, {
	initComponent : function(){
		CMDBuild.CMDBuildCombo.superclass.initComponent.call(this);
        this.on('focus', function(){
        	this.store.clearFilter();
        }, this);
        this.store.on('load', this.growSizeFix, this);        
    },

    getReadableValue: function() {
    	return this.getRawValue();
    }
});