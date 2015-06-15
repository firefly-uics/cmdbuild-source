(function() {

	Ext.define('CMDBuild.model.localizations.advancedTable.ClassesTreeStore', {
		extend: 'CMDBuild.model.localizations.advancedTable.BaseTreeStore',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.proxy.CMProxyConstants.OBJECT, type: 'string'}, // Translated object label to display in grid column
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROPERTY, type: 'string' }, // Translated object property name
			{ name: CMDBuild.core.proxy.CMProxyConstants.WAS_EMPTY, type: 'boolean', defaultValue: true } // Flag to check initial value of translation
		]
	});

})();