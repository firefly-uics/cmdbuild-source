(function() {

	Ext.define('CMDBuild.model.localizations.advancedTable.TreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.CMProxyConstants.OBJECT, type: 'string'}, // Translated object name, label to display in grid column
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROPERTY, type: 'string' }, // Translated object property name
			{ name: CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, type: 'boolean', defaultValue: true } // Flag to check initial value of translation
		],

		/**
		 * Complete fields configuration with all configured languages
		 */
		constructor: function() {
			var modelFields = CMDBuild.model.localizations.advancedTable.TreeStore.getFields();
			var languages = CMDBuild.configuration[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES);

			Ext.Object.each(languages, function(key, value, myself) {
				modelFields.push({ name: value.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
			}, this);

			CMDBuild.model.localizations.advancedTable.TreeStore.setFields(modelFields);

			this.callParent(arguments);
		},

		/**
		 * Checks if languages attributes are empty
		 *
		 * @return {Boolean}
		 */
		isEmpty: function() {
			var returnValue = true;

			Ext.Object.each(this.getData(), function(key, value, myself) {
				if (
					Ext.Array.contains(CMDBuild.configuration[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_TAGS), key)
					&& !Ext.isEmpty(value)
				) {
					returnValue = false;
				}
			}, this);

			return returnValue;
		}
	});

})();