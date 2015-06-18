(function() {

	Ext.define('CMDBuild.model.localizations.advancedTable.TreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.Constants.ENTITY_IDENTIFIER, type: 'string' }, // Node's entity identifier to avoid to be lock to displayed label value
			{ name: CMDBuild.core.proxy.Constants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.Constants.PROPERTY_IDENTIFIER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string'}, // Label to display in grid's tree column
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
		}
	});

})();