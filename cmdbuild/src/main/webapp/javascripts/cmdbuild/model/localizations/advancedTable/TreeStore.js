(function() {

	Ext.define('CMDBuild.model.localizations.advancedTable.TreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENTITY_IDENTIFIER, type: 'string' }, // Node's entity identifier to avoid to be lock to displayed label value
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROPERTY_IDENTIFIER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEXT, type: 'string'}, // Label to display in grid's tree column
		],

		/**
		 * Complete fields properties with all configured languages
		 */
		constructor: function() {
			var modelFields = CMDBuild.model.localizations.advancedTable.TreeStore.getFields();
			var languages = CMDBuild.configuration[CMDBuild.core.proxy.CMProxyConstants.LOCALIZATION].getAllLanguages();

			Ext.Object.each(languages, function(key, value, myself) {
				modelFields.push({ name: value.get(CMDBuild.core.proxy.CMProxyConstants.TAG), type: 'string' });
			}, this);

			CMDBuild.model.localizations.advancedTable.TreeStore.setFields(modelFields);

			this.callParent(arguments);
		}
	});

})();