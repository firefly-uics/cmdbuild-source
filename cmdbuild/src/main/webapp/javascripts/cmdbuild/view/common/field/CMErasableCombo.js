Ext.define("CMDBuild.field.ErasableCombo", {
	extend: "Ext.form.field.ComboBox",
	alias: "cmerasablecombo",
	trigger1cls: Ext.form.field.ComboBox.triggerCls,
	trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
	hideTrigger1 :false,
	hideTrigger2 :false,
	onTrigger1Click: Ext.form.field.ComboBox.prototype.onTriggerClick,
	onTrigger2Click: function() {
		if (!this.disabled) {
			this.clearValue();
		}
	}
});