(function() {

	Ext.require('CMDBuild.core.proxy.Constants');

	Ext.define('CMDBuild.model.email.Accounts.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.email.Accounts.singleAccount', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ADDRESS, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.IMAP_PORT, type: 'int', defaultValue: 1 },
			{ name: CMDBuild.core.proxy.Constants.IMAP_SERVER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.IMAP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.OUTPUT_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.PASSWORD, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SMTP_PORT, type: 'int', defaultValue: 1 },
			{ name: CMDBuild.core.proxy.Constants.SMTP_SERVER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SMTP_SSL, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.USERNAME, type: 'string' }
		]
	});

})();