Ext.define("CMDBuild.field.CMBaseCombo", {
	extend: "Ext.form.field.ComboBox",

	initComponent : function() {
		this.callParent(arguments);

		// TODO 3 to 4 why?
//		this.mon(this, 'focus', function() {
//			this.store.clearFilter();
//		}, this);

		this.mon(this.store, 'load', this.growSizeFix, this);
	},

	growSizeFix: function() {
		//TODO 3 to 4 implement me
	},

	// used by the template resolver to know if a field is a combo
	// and to take the value of multilevel lookup
	getReadableValue: function() {
		return this.getRawValue();
	}
});