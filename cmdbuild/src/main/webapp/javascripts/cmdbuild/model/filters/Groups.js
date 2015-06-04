(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.filters.Groups', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONFIGURATION, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENTRY_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, type: 'boolean', defaultValue: true } // Client managed filters are true
		]
	});

})();