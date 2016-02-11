(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.Localizations.sectionClassesTreeStore', {
		extend: 'Ext.data.TreeModel',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.CMProxyConstants.OBJECT, type: 'string'}, // Translated object label to display in grid column
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROPERTY, type: 'string' }, // Translated object property name
			{ name: 'wasEmpty', type: 'boolean', defaultValue: true } // Flag to check initial value of translation
		],

		/**
		 * Complete fields configuration with all configured languages
		 */
		constructor: function() {
			var modelFields = CMDBuild.model.Localizations.sectionClassesTreeStore.getFields();
			var languages = CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES);

			Ext.Array.forEach(languages, function(language, i, allLanguages) {
				modelFields.push({ name: language.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
			}, this);

			CMDBuild.model.Localizations.sectionClassesTreeStore.setFields(modelFields);

			this.callParent(arguments);
		},

//		/**
//		 * @return {Object}
//		 */
//		getLocalizationsDataObject: function() {
//			var returnValue = {};
//
//			Ext.Object.each(this.getData(), function(key, value, myself) {
//				if (
//					Ext.Array.contains(CMDBuild.Config.localization.get(CMDBuild.core.proxy.CMProxyConstants.LANGUAGES_TAGS), key)
//					&& !Ext.isEmpty(value)
//				) {
//					returnValue[key] = value;
//				}
//			}, this);
//
//			return returnValue;
//		},

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

	Ext.define('CMDBuild.model.Localizations.translation', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TAG, type: 'string' }
		]
	});

})();