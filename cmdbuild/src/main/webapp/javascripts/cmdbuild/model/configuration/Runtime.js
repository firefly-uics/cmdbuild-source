(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.configuration.Runtime', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE, type: 'boolean', defaultValue: true }, // a.k.a. AllowsPasswordLogin
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_GROUP_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_GROUP_ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.GROUPS, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.STARTING_CLASS_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.USER_ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.USERNAME, type: 'string' }
		]
	});

})();