(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.Classes.domainsTreePanel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENABLED, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
		]
	});

})();