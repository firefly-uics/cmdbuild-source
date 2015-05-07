(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.email.Accounts.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.email.Accounts.singleAccount', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMAP_PORT, type: 'int', defaultValue: 1 },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SERVER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMAP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.OUTPUT_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SMTP_PORT, type: 'int', defaultValue: 1 },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SERVER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SMTP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.USERNAME, type: 'string' }
		]
	});

})();