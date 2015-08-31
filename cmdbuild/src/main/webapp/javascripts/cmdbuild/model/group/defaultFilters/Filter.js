(function() {

	Ext.define('CMDBuild.model.group.defaultFilters.Filter', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONFIGURATION, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENTRY_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'boolean' }
		]
	});

})();