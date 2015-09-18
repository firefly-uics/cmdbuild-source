(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.Report', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.GROUP, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEXT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.Report.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.GROUPS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.QUERY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TITLE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' }
		]
	});

})();