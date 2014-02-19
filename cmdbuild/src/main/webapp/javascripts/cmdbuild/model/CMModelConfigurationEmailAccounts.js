(function() {

	Ext.define('CMDBuild.model.configuration.email.accounts.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'address', type: 'string' },
			{ name: 'id', type: 'int' },
			{ name: 'isDefault', type: 'boolean' },
			{ name: 'name', type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.configuration.email.accounts.singleAccount', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'address', type: 'string' },
			{ name: 'enableMoveRejectedNotMatching', type: 'boolean' },
			{ name: 'id', type: 'int' },
			{ name: 'imapPort', type: 'int' },
			{ name: 'imapServer', type: 'string' },
			{ name: 'imapSsl', type: 'boolean' },
			{ name: 'incomingFolder', type: 'string' },
			{ name: 'isDefault', type: 'boolean' },
			{ name: 'password', type: 'string' },
			{ name: 'processedFolder', type: 'string' },
			{ name: 'rejectedFolder', type: 'string' },
			{ name: 'smtpPort', type: 'int' },
			{ name: 'smtpServer', type: 'string' },
			{ name: 'smtpSsl', type: 'boolean' },
			{ name: 'name', type: 'string' },
			{ name: 'username', type: 'string' }
		]
	});

})();