(function() {

	Ext.define('CMDBuild.model.CMModelEmailAccounts.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.CMModelEmailAccounts.singleAccount', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENABLE_MOVE_REJECTED_NOT_MATCHING, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMAP_PORT, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SERVER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.INCOMING_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESSED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.REJECTED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SMTP_PORT, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SERVER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.USERNAME, type: 'string' }
		]
	});

})();