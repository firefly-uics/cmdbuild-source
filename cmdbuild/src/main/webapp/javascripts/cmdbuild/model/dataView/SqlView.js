(function() {

	Ext.define('CMDBuild.model.dataView.SqlView', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.INPUT, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.CMProxyConstants.OUTPUT, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEXT, type: 'string' }
		]
	});

})();