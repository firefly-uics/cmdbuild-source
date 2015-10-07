(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.DataView', {
		alternativeClassName: 'CMDBuild.model.CMDataViewModel',
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_FUNCTION, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' }
		]
	});

})();