(function() {

	Ext.define('CMDBuild.model.report.Grid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.GROUPS, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.QUERY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TITLE, type: 'string' }, // Usually called name
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();