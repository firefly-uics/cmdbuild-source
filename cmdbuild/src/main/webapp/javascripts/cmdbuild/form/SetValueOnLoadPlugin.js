Ext.ns('CMDBuild');

/**
 * Resets the combobox value after the store is loaded
 */

CMDBuild.SetValueOnLoadPlugin = function(config) {
    Ext.apply(this, config);
};

Ext.extend(CMDBuild.SetValueOnLoadPlugin, Ext.util.Observable, {
    init: function(field) {
		field.valueNotFoundText = "";//CMDBuild.Translation.common.loading;
		field.store.on('load', function() {
			this.valueNotFoundText = this.initialConfig.valueNotFoundText;
			if (this.store) {
				//the store is null if the field is not rendered
				this.setValue(this.getValue());
			}
		}, field);
    }
});
