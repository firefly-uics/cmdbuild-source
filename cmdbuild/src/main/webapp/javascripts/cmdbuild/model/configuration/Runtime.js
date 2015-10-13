(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.configuration.Runtime', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE, type: 'boolean', defaultValue: true }, // a.k.a. AllowsPasswordLogin
			{ name: CMDBuild.core.constants.Proxy.GROUPS, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.USERNAME, type: 'string' }
		]
	});

})();