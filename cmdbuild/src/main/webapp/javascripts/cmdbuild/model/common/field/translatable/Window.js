(function() {

	Ext.define('CMDBuild.model.common.field.translatable.Window', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [],

		/**
		 * Complete fields properties with all configured languages
		 */
		constructor: function() {
			var modelFields = [];
			var languages = CMDBuild.configuration[CMDBuild.core.constants.Proxy.LOCALIZATION].getEnabledLanguages();

			Ext.Object.each(languages, function(key, value, myself) {
				modelFields.push({ name: value.get(CMDBuild.core.constants.Proxy.TAG), type: 'string' });
			}, this);

			CMDBuild.model.common.field.translatable.Window.setFields(modelFields);

			this.callParent(arguments);
		},

		/**
		 * @returns {Boolean}
		 */
		isEmpty: function() {
			var result = false;

			result = !Ext.Array.some(Ext.Object.getValues(this.getData()), function(value, i, allValues) { // Returns true at first non empty property
				return !Ext.isEmpty(value);
			}, this);

			return result;
		}
	});

})();