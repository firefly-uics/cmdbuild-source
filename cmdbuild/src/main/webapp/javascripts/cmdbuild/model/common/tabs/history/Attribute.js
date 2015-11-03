(function() {

	Ext.define('CMDBuild.model.common.tabs.history.Attribute', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CHANGED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.INDEX, type: 'int', useNull: true }
		]
	});

})();