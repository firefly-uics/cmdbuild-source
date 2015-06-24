(function() {

	Ext.define('CMDBuild.model.localizations.advancedTable.TreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.Constants.FIELD, type: 'string' }, // Field to translate (description, inverseDescription, ...)
			{ name: CMDBuild.core.proxy.Constants.IDENTIFIER, type: 'string' }, // Entity's attribute/property identifier
			{ name: CMDBuild.core.proxy.Constants.OWNER, type: 'string' }, // Translation owner identifier (className, domainName, ...) used only to translate attribute's entities
			{ name: CMDBuild.core.proxy.Constants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string'}, // Label to display in grid's tree column (usually name attribute)
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string'} // Entity type identifier (class, attributeclass, domain, attributedomain, filter, instancename, lookupvalue, menuitem, report, view, classwidget)
			],

		/**
		 * Complete fields properties with all configured languages
		 */
		constructor: function() {
			var modelFields = CMDBuild.model.localizations.advancedTable.TreeStore.getFields();
			var languages = CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].getAllLanguages();

			Ext.Object.each(languages, function(key, value, myself) {
				modelFields.push({ name: value.get(CMDBuild.core.proxy.Constants.TAG), type: 'string' });
			}, this);

			CMDBuild.model.localizations.advancedTable.TreeStore.setFields(modelFields);

			this.callParent(arguments);
		},

		/**
		 * @returns {Object} translationsObject
		 */
		getTranslations: function() {
			var translationsObject = {};
			var enabledLanguages = CMDBuild.configuration[CMDBuild.core.proxy.Constants.LOCALIZATION].getEnabledLanguages();

			Ext.Object.each(enabledLanguages, function(key, value, myself) {
				translationsObject[key] = this.get(key);
			}, this);

			return translationsObject;
		}
	});

})();