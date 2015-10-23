(function() {

	Ext.define('CMDBuild.model.common.field.searchWindow.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENTRY_TYPE, type: 'auto', defaultValue: {} }, // {CMDBuild.cache.CMEntryTypeModel}
			{ name: CMDBuild.core.proxy.CMProxyConstants.GRID_CONFIGURATION, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.proxy.CMProxyConstants.READ_ONLY, type: 'boolean', defaultValue: true }
		]
	});

})();