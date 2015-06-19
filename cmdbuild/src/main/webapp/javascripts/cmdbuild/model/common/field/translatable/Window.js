(function() {

	Ext.define('CMDBuild.model.common.field.translatable.Window', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [],

		/**
		 * Complete fields properties with all configured languages
		 */
		constructor: function() {
			var modelFields = [];
			var languages = CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].getEnabledLanguages();

			Ext.Object.each(languages, function(key, value, myself) {
				modelFields.push({ name: value.get(CMDBuild.core.proxy.Constants.TAG), type: 'string' });
			}, this);

			CMDBuild.model.common.field.translatable.Window.setFields(modelFields);

			this.callParent(arguments);
		}
	});

})();