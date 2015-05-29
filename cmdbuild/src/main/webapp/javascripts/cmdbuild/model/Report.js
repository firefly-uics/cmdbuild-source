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

	/**
	 * @management
	 */
	Ext.define('CMDBuild.model.Report.createParameters', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EXTENSION, type: 'string', defaultValue: CMDBuild.core.proxy.CMProxyConstants.PDF },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();