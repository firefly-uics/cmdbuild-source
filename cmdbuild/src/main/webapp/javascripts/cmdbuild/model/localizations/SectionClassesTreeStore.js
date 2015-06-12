(function() {

	Ext.define('CMDBuild.model.localizations.SectionClassesTreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.CMProxyConstants.OBJECT, type: 'string'}, // Translated object label to display in grid column
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROPERTY, type: 'string' }, // Translated object property name
			{ name: CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, type: 'boolean', defaultValue: true } // Flag to check initial value of translation
		],

		/**
		 * Complete fields configuration with all configured languages
		 */
		constructor: function() {
			var modelFields = CMDBuild.model.localizations.SectionClassesTreeStore.getFields();
			var languages = CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES);

			Ext.Array.forEach(languages, function(language, i, allLanguages) {
				modelFields.push({ name: language.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
			}, this);

			CMDBuild.model.localizations.SectionClassesTreeStore.setFields(modelFields);

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
					Ext.Array.contains(CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_TAGS), key)
					&& !Ext.isEmpty(value)
				) {
					returnValue = false;
				}
			}, this);

			return returnValue;
		}
	});

})();